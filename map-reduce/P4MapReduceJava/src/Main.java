import java.io.*;

import java.net.MalformedURLException;
import java.net.URL;

import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.Comparator;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class Main {

    /**
     * Entry point in the application.
     * @param args Command-line arguments.
     */
    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Invalid number of arguments given!");
            System.exit(1);
        }

        String inputFilename = args[0];
        String outputFilename = args[1];
        Integer numberWords = Integer.parseInt(args[2]);

        try {
            getWordCount(inputFilename, outputFilename, numberWords);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Read in the Stream of URLs from the input file, one per line.
     * @param inputFilename The filename to read URLs from.
     * @return A Stream of URL strings.
     * @throws IOException If the input file could not be read.
     */
    private static Stream<String> readURLsFromFile(String inputFilename) throws IOException {
        return Files.lines(Paths.get(inputFilename));
    }

    /**
     * Read HTML data from a given URL.
     * @param url The URL to read data from.
     * @return The HTML String.
     */
    private static String readLinesFromURL(String url) {
        URL website;
        BufferedReader in;

        try {
            website = new URL(url);
        } catch (MalformedURLException e) {
            return null;
        }

        try {
            in = new BufferedReader(new InputStreamReader(website.openStream()));
        } catch (IOException e) {
            return null;
        }

        return in.lines().collect(Collectors.joining("\n"));
    }

    /**
     * Download HTML data from all URLs listed in a given file.
     * @param inputFilename The file to read URLs from.
     * @return A Stream of HTML data strings.
     * @throws IOException If the input file could not be read.
     */
    private static Stream<String> downloadWebpageData(String inputFilename) throws IOException {
        Stream<String> urls = readURLsFromFile(inputFilename);
        return urls.map(Main::readLinesFromURL);
    }

    /**
     * Get the word count from the HTML data from a list of URLs listed in a given input file.
     * @param inputFilename The input file with a list of URLs.
     * @param outputFilename The file to write the word counts to.
     * @param numberOfWords The number of words to show in the file.
     * @throws IOException If the input file could not be read or the output file could not be written to.
     */
    private static void getWordCount(String inputFilename, String outputFilename, Integer numberOfWords) throws IOException {
        Stream<String> webpageData = downloadWebpageData(inputFilename);

        Map<String, Integer> wordCounts = webpageData
                .map(Pattern.compile("<style>.*</style>")::matcher) // Get rid of internal CSS
                .map(matcher -> matcher.replaceAll(""))
                .map(Pattern.compile("<.*/?>")::matcher) // Get rid of any < > and < /> tags
                .map(matcher -> matcher.replaceAll(""))
                .map(Pattern.compile("</.*>")::matcher) // Get rid of any </ > tags
                .map(matcher -> matcher.replaceAll(""))
                .map(Pattern.compile("\\p{Punct}")::matcher) // Get rid of all punctuation
                .map(matcher -> matcher.replaceAll(""))
                .flatMap(Pattern.compile("\\s+")::splitAsStream) // Split lines by spaces
                .filter(Pattern.compile("^\\w+$").asPredicate()) // Filter out improper words
                .map(s -> s.toLowerCase()) // Convert all words to lowercase
                .collect(Collectors.groupingBy(w -> w, Collectors.summingInt(w -> 1)));

        File file = new File(outputFilename);
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));

        wordCounts
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(numberOfWords)
                .forEach(e -> {
                    String line = e.getKey() + ": " + e.getValue();
                    System.out.println(line);
                    try {
                        bw.write(line + "\n");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                });

        bw.flush();
    }
}
