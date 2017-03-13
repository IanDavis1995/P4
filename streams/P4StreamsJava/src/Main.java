import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class Main {

    /**
     * Entry point of the application. Parse command line arguments and call filterLogFile.
     * @param args: Command-line arguments.
     * @throws IOException: If the inputFilename given could not be read or the outputFilename given could not be written.
     */
    public static void main(String[] args) throws IOException {
        String ifnm = "/var/log/auth.log", ofnm = "/tmp/invalidUsers.txt";

        switch (args.length) {
            case 0: break;
            case 2: ofnm = args[ 1]; // fall through
            case 1: ifnm = args[ 0]; break;
            default:
                System.out.println("Usage: At most two file names expected");
                System.exit(0);
        }

        filterLogFile(ifnm, ofnm);
    }

    /**
     * Read the given input file into a Stream of strings representing each line of the file.
     * @param inputFilename: The filename to read from.
     * @return A Stream of strings representing each line of the file.
     * @throws IOException: If the filename given could not be read.
     */
    private static Stream<String> readLogFile(String inputFilename) throws IOException {
        return Files.lines(Paths.get(inputFilename));
    }

    /**
     * Write a Stream of Strings representing a single line of the file to the given filename.
     * @param outputFilename The filename to write to.
     * @param outputLines The stream of strings representing each line to write to the file.
     * @throws IOException If the file could not be written to.
     */
    private static void writeOutputFile(String outputFilename, Stream<String> outputLines) throws IOException {
        Files.write(Paths.get(outputFilename), ((Iterable<String>) outputLines::iterator));
    }

    /**
     * Filter the log file given at inputFilename and write the properly filtered lines to the file at outputFilename.
     * Precondition: The file pointed to by inputFilename is an auth.log file on the system, with log messages from sshd
     * Postcondition: The file pointed to by outputFilename exists, and contains messages from the input file that
     *                  contain the text "Invalid User"
     * @param inputFilename The filename of the log file to read from.
     * @param outputFilename The filename to write the filtered lines to.
     */
    private static void filterLogFile(String inputFilename, String outputFilename) {
        Stream<String> inputLines;
        Stream<String> outputStream;
        List<String> fileLines;
        List<String> outputLines;

        try {
            inputLines = readLogFile(inputFilename);
        } catch (IOException e) {
            return;
        }

        fileLines = inputLines.collect(Collectors.toList());
        inputLines = fileLines.stream();

        // Filter out lines that don't contain the text "Invalid user"
        outputStream = inputLines.filter(line -> {
            System.out.println(line);
            return line.contains("Invalid user");
        });

        // Parse out the irrelevant parts of each log message and store a new stream with just the hostname.
        outputStream = outputStream.map(line -> {
            String usefulHalf = line.split("sshd\\[.*\\]")[1];
            String[] tokens = usefulHalf.split(" ");
            return tokens[5]; // The fourth index in each line should be the hostname.
        });

        outputLines = outputStream.collect(Collectors.toList());
        outputStream = outputLines.stream();

        try {
            writeOutputFile(outputFilename, outputStream);
        } catch (IOException e) {

        }
    }
}
