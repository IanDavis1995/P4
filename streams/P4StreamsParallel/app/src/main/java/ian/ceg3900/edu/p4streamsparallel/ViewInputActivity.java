package ian.ceg3900.edu.p4streamsparallel;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;


public class ViewInputActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_input);
        TextView inputView = (TextView) findViewById(R.id.view_input_text);

        if (MainActivity.fileLines == null) {
            new AlertDialog.Builder(this)
                    .setTitle("File read failed")
                    .setMessage("The log file could not be found/read!")
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
            return;
        }

        for (String line: MainActivity.fileLines) {
            inputView.append(line + "\n");
        }
    }
}
