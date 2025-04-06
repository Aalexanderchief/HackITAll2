# import speech_recognition as sr

# # Initialize the recognizer
# r = sr.Recognizer()

# # File to save the recognized text
# output_file = "recognized_text.txt"

# # List all available microphone devices
# print("Available microphone devices:")
# mic_list = sr.Microphone.list_microphone_names()
# for i, mic_name in enumerate(mic_list):
#     print(f"{i}: {mic_name}")

# # Specify the device index (replace with the desired index)
# device_index = int(input("Enter the device index you want to use: "))

# print("Starting microphone recording. Speak into the microphone...")

# # Main loop to capture and save audio
# try:
#     with sr.Microphone(device_index=device_index) as source:
#         # Adjust for ambient noise
#         print("Adjusting for ambient noise... Please wait.")
#         r.adjust_for_ambient_noise(source, duration=1)
#         print("Listening for 5 seconds...")

#         # Listen for the user's input for 5 seconds
#         try:
#             audio = r.listen(source, timeout=5, phrase_time_limit=5)
#         except sr.WaitTimeoutError:
#             print(
#                 "Listening timed out while waiting for phrase to start. Please try again."
#             )
#             exit()
#         print("Processing audio...")

#         # Recognize speech using Google Speech Recognition
#         recognized_text = r.recognize_google(audio)
#         print(f"You said: {recognized_text}")

#         # Save the recognized text to a file
#         with open(output_file, "a") as file:
#             file.write(recognized_text + "\n")
#         print(f"Recognized text saved to {output_file}")

# except sr.RequestError as e:
#     print(f"Could not request results from Google Speech Recognition service; {e}")
# except Exception as e:
#     print(
#         f"An unexpected error occurred: {e}. Ensure your microphone is properly connected and accessible."
#     )
#     print("Sorry, I could not understand the audio.")


import speech_recognition as sr

r = sr.Recognizer()
mic = sr.Microphone(device_index=1)  # try index 1, 4 or 5

with mic as source:
    print("🎙️ Adjusting for ambient noise...")
    r.adjust_for_ambient_noise(source)
    print("🎧 Recording for 5 seconds...")
    audio = r.record(source, duration=5)

    # save the audio to a file
    with open("recorded_audio.wav", "wb") as f:
        f.write(audio.get_wav_data())

    print("🧠 Transcribing...")
    try:
        text = r.recognize_google(audio)
        print("📄 Transcript:", text)
    except Exception as e:
        print("❌ Error:", e)