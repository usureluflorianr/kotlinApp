import requests
import json

url = "https://api.openai.com/v1/images/generations"

headers = {
  "Content-Type": "application/json",
  "Authorization": "Bearer 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"
}

data = {
  "prompt": "a green truck",
  "n": 1,
  "size": "1024x1024"
}

response = requests.post(url, headers=headers, json=data)

data = response.json()

# Extract the URL from the data
url = data['data'][0]['url']

response = requests.get(url)

if response.status_code == 200:
  with open("image.jpg", "wb") as f:
    f.write(response.content)


