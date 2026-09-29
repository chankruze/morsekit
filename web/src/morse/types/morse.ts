export type EncodeResult = {
  /** Canonical notation, e.g. `... --- ...`. */
  morse: string;
  /** Characters with no Morse code; they're left out of `morse`. */
  unsupported: string[];
};

export type DecodeResult = {
  text: string;
  /** Tokens that aren't a known letter; each became the replacement character. */
  unreadable: string[];
};

export type Tone = {
  /** Seconds from the start. */
  start: number;
  duration: number;
};

export type ToneSchedule = {
  tones: Tone[];
  /** Seconds, up to the end of the last tone. */
  duration: number;
};

export type PlayMorseOptions = {
  wpm?: number;
  toneHz?: number;
  onEnd?: () => void;
};
