import requests
import json

# Set the API endpoint URL
url = "https://api.openai.com/v1/completions"

# Set the API key
api_key = "YOUR_API_KEY"

# Set the request headers
headers = {
  "Content-Type": "application/json",
  "Authorization": "Bearer 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"
}
# Set the request payload
data = {
  "model": "text-davinci-003",
  "prompt": "give me a name for a cat",
  "max_tokens": 1000,
  "temperature": 0
}

# Send the request
response = requests.post(url, headers=headers, json=data)

response_data = response.json()

# Access the data returned by the API
completions = response_data['choices'][0]['text']
with open("mesaj.txt", "w") as f:
  # Write the text to the file
  f.write(completions)