package ian.ceg3900.edu.p4streams;

import android.content.Intent;

import android.os.Environment;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;

import android.view.View;

import android.widget.Button;

import java.io.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    static List<String> fileLines;
    static List<String> outputLines;

    private Button viewInputButton;
    private Button viewOutputButton;

    /**
     * Called by Android SDK when the Activity first starts.
     * @param savedInstanceState Contains information about the state the activity was previously in.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        filterLogFile();

        viewInputButton = (Button) findViewById(R.id.view_input_button);
        viewOutputButton = (Button) findViewById(R.id.view_output_button);

        viewInputButton.setOnClickListener(this);
        viewOutputButton.setOnClickListener(this);
    }

    /**
     * Handle button clicks from the user.
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        if (v == viewInputButton) {
            onViewInputClicked();
        } else if (v == viewOutputButton) {
            onViewOutputClicked();
        }
    }

    /**
     * Handle a click on the View Input button.
     * Launch the View Input activity.
     */
    private void onViewInputClicked() {
        Intent i = new Intent(this, ViewInputActivity.class);
        startActivity(i);
    }

    /**
     * Handle a click on the View Output button.
     * Launch the View Output activity.
     */
    private void onViewOutputClicked() {
        Intent i = new Intent(this, ViewOutputActivity.class);
        startActivity(i);
    }

    /**
     * Read in data from the log file resource, and return a Stream of Strings representing each line of the file.
     * @return A Stream of Strings of each line of the file.
     * @throws FileNotFoundException If the log file resource was not found on the system.
     */
    private Stream<String> readLogFile() throws FileNotFoundException {
        InputStream is = this.getResources().openRawResource(R.raw.auth);
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        return br.lines();
    }

    /**
     * Write a Stream of output lines to a given file in the download directory of our application's data directory.
     * @param outputFilename The name of the file to write to.
     * @param outputLines The stream of output line sto write.
     * @throws IOException If the file could not be opened or written to.
     */
    private void writeOutputFile(String outputFilename, Stream<String> outputLines) throws IOException {
        File root = this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(root, outputFilename);

        BufferedWriter bw = new BufferedWriter(new FileWriter(file));

        outputLines.forEach((outputLine) -> {
            try {
                bw.write(outputLine + "\n");
            } catch (IOException e) {

            }
        });

        bw.flush();
    }

    /**
     * Filter out the useful lines of the given output stream and write the result to the given filename.
     * @param outputFilename The name of the file to write to, will be placed in the application's download directory.
     * @param outputStream A Stream of lines containing the original log file data.
     */
    private void parseOutputStream(String outputFilename, Stream<String> outputStream) {
        AtomicInteger index = new AtomicInteger();

        // Parse out the irrelevant parts of each log message and store a new stream with just the hostname.
        outputLines = outputStream.map(line -> {
            String usefulHalf = line.split("sshd\\[.*\\]")[1];
            String[] tokens = usefulHalf.split(" ");
            Integer currentInteger = index.getAndIncrement();
            // The fourth index in each line should be the hostname.
            return currentInteger.toString() + " " + tokens[5];
        }).collect(Collectors.toList());

        try {
            writeOutputFile(outputFilename, outputLines.stream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Filter the log file given at inputFilename and write the properly filtered lines to the file at outputFilename.
     * Precondition: There is a resource file named auth.log in this apk containing log messages from sshd
     * Postcondition: The file named invalidusers.txt exists in the application's download directory,
     *      and contains messages from the resource file that contain the text "Invalid User"
     */
    private void filterLogFileParallel() {
        String outputFilename = "invalidUsers.txt";

        Stream<String> inputLines;
        Stream<String> outputStream;

        try {
            inputLines = readLogFile().parallel();
        } catch (FileNotFoundException e) {
            return;
        }

        fileLines = inputLines.collect(Collectors.toList());
        inputLines = fileLines.parallelStream();
        outputStream = inputLines.filter(line -> line.contains("Invalid user"));
        parseOutputStream(outputFilename, outputStream);
    }

    /**
     * Filter the log file given at inputFilename and write the properly filtered lines to the file at outputFilename.
     * Precondition: There is a resource file named auth.log in this apk containing log messages from sshd
     * Postcondition: The file named invalidusers.txt exists in the application's download directory,
     *      and contains messages from the resource file that contain the text "Invalid User"
     */
    private void filterLogFile() {
        String outputFilename = "invalidusers.txt";

        Stream<String> inputLines;
        Stream<String> outputStream;

        try {
            inputLines = readLogFile();
        } catch (FileNotFoundException e) {
            return;
        }

        fileLines = inputLines.collect(Collectors.toList());
        inputLines = fileLines.stream();
        outputStream = inputLines.filter(line -> line.contains("Invalid user"));

        parseOutputStream(outputFilename, outputStream);
    }
}
