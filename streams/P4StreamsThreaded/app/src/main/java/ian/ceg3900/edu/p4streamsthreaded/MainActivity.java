package ian.ceg3900.edu.p4streamsthreaded;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        filterLogFileThreaded();

        viewInputButton = (Button) findViewById(R.id.view_input_button);
        viewOutputButton = (Button) findViewById(R.id.view_output_button);

        viewInputButton.setOnClickListener(this);
        viewOutputButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == viewInputButton) {
            onViewInputClicked();
        } else if (v == viewOutputButton) {
            onViewOutputClicked();
        }
    }

    private void onViewInputClicked() {
        Intent i = new Intent(this, ViewInputActivity.class);
        startActivity(i);
    }

    private void onViewOutputClicked() {
        Intent i = new Intent(this, ViewOutputActivity.class);
        startActivity(i);
    }

    private Stream<String> readLogFile() throws FileNotFoundException {
        InputStream is = this.getResources().openRawResource(R.raw.auth);
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        return br.lines();
    }

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
     * Postcondition: A thread is started that will run the filterLogFile function.
     */
    private void filterLogFileThreaded() {
        Thread filterThread = new Thread(this::filterLogFile);
        filterThread.start();
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
