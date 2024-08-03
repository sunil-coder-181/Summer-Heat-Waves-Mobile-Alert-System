#This Flask Server was Deployed in the Render Website
from flask import Flask, request, jsonify
import requests
import joblib
import numpy as np
import pandas as pd
from sklearn.preprocessing import MinMaxScaler
import firebase_admin
from firebase_admin import credentials, messaging

app = Flask(__name__)

# Load the trained model and scaler
model = joblib.load('heat_wave_model.pkl')
scaler = joblib.load('scaler.pkl')  # Ensure you save the scaler during preprocessing

# Initialize Firebase Admin SDK
def initialize_firebase():
    if not firebase_admin._apps:
        cred = credentials.Certificate('android-e489f-firebase-adminsdk-9zb83-088889d4e9.json')#Change the Firebase Admin Sdk File for running this Flask Server
        firebase_admin.initialize_app(cred)

initialize_firebase()

def send_fcm_notification(token, title, body):
    message = messaging.Message(
        notification=messaging.Notification(
            title=title,
            body=body,
        ),
        token=token,
    )
    
    response = messaging.send(message)
    return response

def fetch_live_weather(api_key, latitude, longitude):
    url = f'https://api.openweathermap.org/data/2.5/weather?lat={latitude}&lon={longitude}&appid={api_key}&units=metric'
    response = requests.get(url)
    data = response.json()
    
    weather_data = {
        'MaxT': data['main']['temp_max'],
        'MinT': data['main']['temp_min'],
        'AvgT': data['main']['temp'],
        'Humidity': data['main']['humidity'],
        'WindSpeed': data['wind']['speed'],
        'heat_index': data['main']['feels_like']
    }
    return weather_data

@app.route('/predict-live-heat-wave', methods=['POST'])
def predict_live_heat_wave():
    data = request.get_json()
    api_key = data.get('api_key')
    latitude = data.get('latitude')
    longitude = data.get('longitude')
    token = data.get('token')
    
    if not api_key or not latitude or not longitude or not token:
        return jsonify({'error': 'Missing api_key, latitude, longitude, or token'}), 400
    
    live_weather_data = fetch_live_weather(api_key, latitude, longitude)
    
    features = ['MaxT', 'MinT', 'WindSpeed', 'Humidity', 'AvgT', 'heat_index']
    live_weather_df = pd.DataFrame([live_weather_data], columns=features)
    
    # Normalize the live data
    live_weather_df[features] = scaler.transform(live_weather_df[features])
    
    # Make prediction
    prediction = model.predict(live_weather_df)
    
    # Send notification if a heat wave is predicted
    if prediction[0] == 1:
        title = 'Heat Wave Alert'
        body = 'A heat wave is predicted in your area. Stay safe!'
        send_fcm_notification(token, title, body)
    
    return jsonify({'prediction': int(prediction[0])})

if __name__ == '__main__':
    app.run(debug=True)
