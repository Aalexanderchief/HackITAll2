import speech_recognition as sr
from transformers import pipeline
import sys

# Set up Hugging Face Whisper pipeline for speech-to-text
pipe = pipeline("automatic-speech-recognition", model="openai/whisper-large")

def transcribe_audio():
    r = sr.Recognizer()
    mic = sr.Microphone()  # Automatically selects the default microphone

    with mic as source:
        print("🎙️ Adjusting for ambient noise...")
        r.adjust_for_ambient_noise(source)
        print("🎧 Listening for speech...")

        try:
            print("🎧 Listening...")
            audio = r.listen(source, timeout=5, phrase_time_limit=5)
            print("🧠 Transcribing...")

            # Convert audio to wav data (bytes)
            audio_data = audio.get_wav_data()

            # Use Hugging Face's pipeline to transcribe the audio directly from the byte data
            result = pipe(audio_data)

            # Extract and print the transcribed text
            text = result['text']
            print(f"📄 Transcript: {text}")

            # Flush stdout to ensure that the output is immediately written
            sys.stdout.write(text)
            sys.stdout.flush()  # This ensures the output is immediately sent to the parent process (Kotlin)

        except Exception as e:
            print("❌ Error:", e)
            sys.stdout.write("Error transcribing audio")
            sys.stdout.flush()  # Ensure error message is sent as well

if __name__ == "__main__":
    transcribe_audio()


# import speech_recognition as sr
# from transformers import pipeline
# import sys
#
# # Set up Hugging Face Whisper pipeline for speech-to-text
# pipe = pipeline("automatic-speech-recognition", model="openai/whisper-large")
#
# def transcribe_audio():
#     r = sr.Recognizer()
#     mic = sr.Microphone()  # Automatically selects the default microphone
#
#     with mic as source:
#         print("🎙️ Adjusting for ambient noise...")
#         r.adjust_for_ambient_noise(source)
#         print("🎧 Listening for speech...")
#
#         try:
#             print("🎧 Listening...")
#             audio = r.listen(source, timeout=5, phrase_time_limit=5)
#             print("🧠 Transcribing...")
#
#             # Convert audio to wav data (bytes)
#             audio_data = audio.get_wav_data()
#
#             # Use Hugging Face's pipeline to transcribe the audio directly from the byte data
#             result = pipe(audio_data)
#
#             # Extract and print the transcribed text
#             text = result['text']
#             print(f"📄 Transcript: {text}")
#             sys.stdout.write(text)  # Output transcription to stdout
#
#         except Exception as e:
#             print("❌ Error:", e)
#             sys.stdout.write("Error transcribing audio")
#
# if __name__ == "__main__":
#     transcribe_audio()


# import speech_recognition as sr
# from transformers import pipeline
#
# # Set up the Hugging Face Whisper pipeline for speech-to-text
# pipe = pipeline("automatic-speech-recognition", model="openai/whisper-large")
#
# def transcribe_audio():
#     r = sr.Recognizer()
#     mic = sr.Microphone()  # Automatically selects the default microphone
#
#     with mic as source:
#         print("🎙️ Adjusting for ambient noise...")
#         r.adjust_for_ambient_noise(source)
#         print("🎧 Listening for speech...")
#
#         try:
#             print("🎧 Listening...")
#             audio = r.listen(source, timeout=5, phrase_time_limit=5)
#             print("🧠 Transcribing...")
#
#             # Convert audio to wav data (bytes)
#             audio_data = audio.get_wav_data()
#
#             # Use Hugging Face's pipeline to transcribe the audio directly from the byte data
#             # The pipeline will automatically handle the audio processing
#             result = pipe(audio_data)
#
#             # Extract and print the transcribed text
#             text = result['text']
#             print(f"📄 Transcript: {text}")
#             return text
#
#         except Exception as e:
#             print("❌ Error:", e)
#             return "Error transcribing audio"
#
# if __name__ == "__main__":
#     transcription = transcribe_audio()
#     print(f"Transcription: {transcription}")
#


# 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

# import speech_recognition as sr
#
# def transcribe_audio():
#     r = sr.Recognizer()
#     mic = sr.Microphone()  # Automatically selects the default microphone
#
#     with mic as source:
#         print("🎙️ Adjusting for ambient noise...")
#         r.adjust_for_ambient_noise(source)
#         print("🎧 Listening for speech...")
#
#         try:
#             print("🎧 Listening...")
#             audio = r.listen(source, timeout=5, phrase_time_limit=5)
#             print("🧠 Transcribing...")
#             # Using Sphinx for transcription (offline)
#             text = r.recognize_sphinx(audio)
#             print(f"📄 Transcript: {text}")
#             return text
#         except Exception as e:
#             print("❌ Error:", e)
#             return "Error transcribing audio"
#
# if __name__ == "__main__":
#     transcription = transcribe_audio()
#     print(f"Transcription: {transcription}")

# import speech_recognition as sr
# import sys
#
# def transcribe_audio():
#     r = sr.Recognizer()
#     mic = sr.Microphone()  # Automatically selects the default microphone
#
#     with mic as source:
#         print("🎙️ Adjusting for ambient noise...")
#         r.adjust_for_ambient_noise(source)
#         print("🎧 Listening for speech...")
#
#         # Listen for speech continuously
#         try:
#             print("🎧 Listening...")
#             audio = r.listen(source, timeout=5, phrase_time_limit=5)
#             print("🧠 Transcribing...")
#             text = r.recognize_google(audio)
#             print(f"📄 Transcript: {text}")
#             return text
#         except Exception as e:
#             print("❌ Error:", e)
#             return "Error transcribing audio"
#
# if __name__ == "__main__":
#     command = sys.argv[1]  # Get command argument (either 'start' or 'stop')
#
#     if command == "start":
#         # Start listening and transcribe
#         transcription = transcribe_audio()
#         sys.stdout.write(transcription)  # Output transcription to stdout


# import speech_recognition as sr
# import sys
#
# def transcribe_audio():
#     r = sr.Recognizer()
#     mic = sr.Microphone(device_index=1)  # Specify the device index if needed
#
#     with mic as source:
#         print("🎙️ Adjusting for ambient noise...")
#         r.adjust_for_ambient_noise(source)
#         print("🎧 Recording for 5 seconds...")
#         audio = r.listen(source, timeout=5, phrase_time_limit=5)
#
#         # Save the audio to a file (optional)
#         with open("recorded_audio.wav", "wb") as f:
#             f.write(audio.get_wav_data())
#
#         print("🧠 Transcribing...")
#
#         try:
#             text = r.recognize_google(audio)
#             print("📄 Transcript:", text)
#             return text
#         except Exception as e:
#             print("❌ Error:", e)
#             return "Error transcribing audio"
#
# if __name__ == "__main__":
#     transcription = transcribe_audio()
#     sys.stdout.write(transcription)  # Output transcription to the stdout
