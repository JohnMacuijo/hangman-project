# Hangman

A JavaFX take on the classic word-guessing game — race the clock, dodge the noose, and if you're feeling bold, take on the Hangman King in a boss-battle Challenge Mode.

## About the Game

Guess the hidden word one letter at a time before the hangman drawing is completed. Every wrong guess brings the drawing one step closer to finished — get the word first and you move straight on to the next one. Play with the on-screen keyboard or just type; both are wired up.

Alongside the classic mode, **Challenge Mode** pits you directly against the Hangman King: a fixed sequence of words, a strict time-and-guess limit on each one, and a prisoner whose fate depends on how well you hold up under pressure.

## How to Play

1. Pick a difficulty (or Challenge Mode) from the main menu.
2. Guess letters using the on-screen keyboard or your physical keyboard.
3. Solve the word before you run out of guesses (and, in some modes, time) to move on automatically.
4. Use **Unlock a Letter** if you're stuck — it reveals one random letter, at the cost of 5 seconds off the clock.
5. Beat your own high score, or beat the King.

## Difficulty Levels

| Difficulty | Time per correct letter    | Feel                             |
| ---------- | -------------------------- | -------------------------------- |
| **Easy**   | More time added per letter | Gentle pace, good for warming up |
| **Medium** | Balanced                   | A fair, steady challenge         |
| **Hard**   | Less time added per letter | Tougher words, tighter clock     |

Each difficulty tracks and saves **its own high score**, so progress on Easy never gets overwritten by a Hard run (or vice versa).

## Challenge Mode — The Hangman King

The Hangman King has captured a prisoner, and he's only willing to let them go one word at a time.

- Solve **10 words** to save the prisoner and win.
- Each word gives you **15 seconds _and_ 13 letter guesses** — whichever runs out first ends that word.
- Failing a word (out of time or out of guesses) advances the hangman drawing one stage closer to completion.
- Reach the final stage, and the King wins — the prisoner's fate is sealed.
- Flavor text from the King taunts you throughout, win or lose.

## Features

### 🏆 Scoring

- Flat **+5 points** per word solved, awarded only on a fully completed word.
- Correct letter guesses add bonus time, scaled by difficulty.
- High score tracked **per difficulty**, saved to disk and updated live the moment it's beaten.

### ⏱️ Timer & Pressure

- Countdown timer with an escalating visual/audio warning as time runs low.
- In Classic Mode, the hangman art itself counts down in the final 10 seconds, separate from the wrong-guess drawing.
- Auto-advance to the next word on a correct answer — no button press needed to keep playing.

### 💡 Unlock a Letter

- Reveals one random unguessed letter for a 5-second time penalty.
- Usable multiple times per word, until nothing's left to reveal.

### 🔊 Audio

- Distinct sound cues for correct guesses, wrong guesses, solved words, and game over.
- Timer tick plays through the entire countdown, growing more urgent as time runs out.
- Challenge Mode's tick rises in pitch as the clock drops, building tension unique to the boss fight.
- Game-over sound stops cleanly when returning to the menu or starting a new game.

### 🖥️ UI / UX

- Dark-themed, consistent visual style across the menu, Classic Mode, and Challenge Mode.
- Hover animations and click feedback on every button.
- "Back to Menu" available from the game-over screen alongside "Play Again."
- Status messages wrap properly and correctly reflect win (green) vs. loss (red).
- Fullscreen preserved consistently across every screen transition.

## Tech

Built with **JavaFX**, using FXML for layout and a shared dark/orange visual theme across all screens.
