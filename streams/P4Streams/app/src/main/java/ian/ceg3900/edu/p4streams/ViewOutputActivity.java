package ian.ceg3900.edu.p4streams;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;


public class ViewOutputActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_output);
        TextView inputView = (TextView) findViewById(R.id.view_output_text);

        if (MainActivity.outputLines == null) {
            new AlertDialog.Builder(this)
                    .setTitle("File read failed")
                    .setMessage("The output file could not be found/read!")
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
            return;
        }

        MainActivity.outputLines.forEach((line) -> inputView.append(line + "\n"));
    }
}
