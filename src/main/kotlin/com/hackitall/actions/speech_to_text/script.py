import numpy as np
import os
import scipy.io.wavfile as wav
import sounddevice as sd
import whisper



def record_audio(filename="output.wav", duration=5, fs=16000): print(
    f"🎙️ Recording for {duration} seconds... Speak now!")


audio = sd.rec(int(duration * fs), samplerate=fs, channels=1, dtype="int16")
sd.wait()
wav.write(filename, fs, audio)
print(f"✅ Recording saved as {filename}")


def transcribe_audio(filename): print("🔍 Transcribing with Whisper...")


model = whisper.load_model(
    "base")  # you can also try "tiny", "small", etc. result = model.transcribe(filename) print("\n📝 Transcription result:\n") print(result["text"]) return result["text"]


def main(): timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")


filename = f"recording_{timestamp}.wav"
duration = int(input("How many seconds do you want to record? (e.g. 5): ").strip() or 5)

record_audio(filename, duration=duration)
transcribe_audio(filename)

if name == "main": main()
