import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

public class Main {
    private static final List<String> FALLBACK_WORDS =
            Arrays.asList("кентавр", "виселица", "клавиатура", "пластик", "стажировка");
    private static final String[] HANGMAN_STAGES = {
            "— — — —\n|     |\n|\n|\n|\n|",
            "— — — —\n|     |\n|     o\n|\n|\n|",
            "— — — —\n|     |\n|     o\n|     O\n|\n|",
            "— — — —\n|     |\n|     o\n|    /O\n|\n|",
            "— — — —\n|     |\n|     o\n|    /O\\\n|\n|",
            "— — — —\n|     |\n|     o\n|    /O\\\n|    /\n|",
            "— — — —\n|     |\n|     o\n|    /O\\\n|    / \\\n|"};
    private static final int MAX_ERRORS = 6;
    private static final String WORDS_FILE_PATH = "resources/words.txt";

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        launchGame(in);
    }

    private static void launchGame(Scanner in) {
        while (true) {
            if (confirmGameStart(in)) {
                playGame(in);
            } else {
                System.out.println("Выход из программы.");
                break;
            }
        }
    }

    private static boolean confirmGameStart(Scanner in) {
        Set<String> YES_SET = Set.of("д", "да", "y", "yes");
        Set<String> NO_SET = Set.of("н", "нет", "n", "no");
        while (true) {
            System.out.print("Начать новую игру? ");
            String input = in.nextLine().trim().toLowerCase(Locale.ROOT);
            if (input.isEmpty()) {
                System.out.println("Вы ничего не ввели!");
                continue;
            }
            if (YES_SET.contains(input)) return true;
            else if (NO_SET.contains(input)) return false;
            else System.out.println("Некорректный ввод.");
        }
    }

    private static void playGame(Scanner in) {
        int errorCount = 0;
        String hiddenWord = getHiddenWord();
        boolean isGameLost = false;
        boolean isGameWon = false;
        Set<Character> rightLetters = new HashSet<>();
        Set<Character> usedLetters = new TreeSet<>();
        while (!isGameLost && !isGameWon) {
            printHangman(errorCount);
            printBoard(hiddenWord, rightLetters);
            if (!usedLetters.isEmpty()) printUsedLetters(usedLetters);
            char suggestedLetter = enterLetter(in);
            boolean isLetterRight = isLetterInWord(hiddenWord, suggestedLetter);
            if (usedLetters.contains(suggestedLetter)) {
                System.out.println("Вы уже указывали эту букву!");
                System.out.println();
                continue;
            }
            else usedLetters.add(suggestedLetter);
            if (!isLetterRight) errorCount++;
            else rightLetters.add(suggestedLetter);
            isGameLost = checkLosing(errorCount);
            isGameWon = checkWinning(hiddenWord, rightLetters);
            System.out.println();
        }
        if (isGameLost) {
            printHangman(errorCount);
            printFinalWords(hiddenWord, false);
        } else {
            printBoard(hiddenWord, rightLetters);
            printFinalWords(hiddenWord, true);
        }
        System.out.println();
    }

    private static String getHiddenWord() {
        List<String> words = loadWords();
        Random random = new Random();
        return words.get(random.nextInt(words.size()));
    }

    private static List<String> loadWords() {
        Path wordsFilePath = Paths.get(WORDS_FILE_PATH);
        try {
            List<String> wordsFromFile = Files.readAllLines(wordsFilePath, StandardCharsets.UTF_8)
                    .stream().map(String::trim).map(String::toLowerCase)
                    .filter(word -> !word.isEmpty()).toList();
            if (wordsFromFile.isEmpty()) throw new IOException();
            return wordsFromFile;

        } catch (IOException e) {
            String wordsAbsolutePath = wordsFilePath.toAbsolutePath().toString();
            System.out.println("Файл со списком слов по пути \"" + wordsAbsolutePath + "\" пуст или не обнаружен. " +
                    "Будут использоваться служебные слова.");
            return FALLBACK_WORDS;
        }
    }

    private static char enterLetter(Scanner in) {
        while (true) {
            System.out.print("Введите предполагаемую букву: ");
            String input = in.nextLine().trim().toLowerCase(Locale.ROOT);
            if (input.isEmpty()) {
                System.out.println("Вы ничего не ввели!");
                continue;
            }
            if (input.length() > 1) {
                System.out.println("Вы ввели больше одного символа!");
                continue;
            }
            char letter = input.charAt(0);
            if ((letter >= 'а' && letter <= 'я') || letter == 'ё') return letter;
            else System.out.println("Вы ввели не русскую букву!");
        }
    }

    private static void printHangman(int errorCount) {
        System.out.println(HANGMAN_STAGES[errorCount]);
        System.out.println();
    }

    private static void printBoard(String hiddenWord, Set<Character> rightLetter) {
        for (char letter : hiddenWord.toCharArray()) {
            if (rightLetter.contains(letter)) System.out.print(letter + " ");
            else System.out.print("_ ");
        }
        System.out.println();
        System.out.println();
    }

    private static boolean isLetterInWord(String hiddenWord, char suggestedLetter) {
        return hiddenWord.indexOf(suggestedLetter) != -1;
    }

    private static void printUsedLetters(Set<Character> usedLetters) {
        System.out.print("Использованные буквы: ");
        for (char letter : usedLetters) {
            System.out.print(letter + " ");
        }
        System.out.println();
    }

    private static boolean checkLosing(int errorCount) {
        return errorCount >= MAX_ERRORS;
    }

    private static boolean checkWinning(String hiddenWord, Set<Character> rightLetters) {
        for (char letter : hiddenWord.toCharArray()) {
            if (!rightLetters.contains(letter)) return false;
        }
        return true;
    }

    private static void printFinalWords(String hiddenWord, boolean isGameWon) {
        if (!isGameWon) System.out.println("Вы проиграли! Загаданное слово: " + hiddenWord + ".");
        else System.out.println("Вы выиграли! Загаданное слово: " + hiddenWord + ".");
    }
}