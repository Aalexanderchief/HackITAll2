import speech_recognition as sr
import pyaudio
import time

r = sr.Recognizer()
mic = sr.Microphone()

def main():
    # Set the energy threshold for voice detection
    # Values between 300-3000 are common, adjust based on your environment
    r.energy_threshold = 1000

    # How long to wait in silence before considering the speech ended (in seconds)
    r.pause_threshold = 1.3

    # Minimum length of silence to consider the phrase complete
    r.non_speaking_duration = 1

    with mic as source:
        print(" Adjusting for ambient noise...")
        r.adjust_for_ambient_noise(source, duration=1)
        print(" Speak now (will stop recording after silence)...")

        try:
            # timeout=None means wait indefinitely for speech to start
            # phrase_time_limit=None means no time limit, will stop on silence
            audio = r.listen(source, timeout=10000, phrase_time_limit=None)

            text = r.recognize_google(audio)
            print(" You said:", text)
            with open("output.txt", "w") as f:
                f.write(text)
        except sr.UnknownValueError:
            print(" Could not understand audio")
        except sr.RequestError as e:
            print(" API error:", e)
        except sr.WaitTimeoutError:
            print(" No speech detected within timeout")

if __name__ == '__main__':
    main()