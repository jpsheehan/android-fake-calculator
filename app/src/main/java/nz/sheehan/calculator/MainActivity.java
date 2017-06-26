package nz.sheehan.calculator;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends FragmentActivity {

    private static final long DEFAULT_MAGIC_NUMBER = 123456789;
    private static final String DEFAULT_MAGIC_SEQUENCE = "**+";

    private long magic_number;
    private String magic_sequence;

    private double storedValue = 0.0;
    private char operation = ' ';
    private Boolean isInDecimal = false;
    private Boolean wasEqualsLast = false;
    private String previousOperations = "";

    private Boolean isMagicActive;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Button buttonDecimal = findViewById(R.id.buttonDecimal);
        buttonDecimal.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                onButtonDecimalLongPress(view);
                return true;
            }
        });

        prefs = getSharedPreferences("nz.sheehan.calculator", Context.MODE_PRIVATE);

        try {
            magic_number = prefs.getLong("nz.sheehan.calculator.magic_number", DEFAULT_MAGIC_NUMBER);
            magic_sequence = prefs.getString("nz.sheehan.calculator.magic_sequence", DEFAULT_MAGIC_SEQUENCE);
        }
        catch (Exception ex) {
            magic_number = DEFAULT_MAGIC_NUMBER;
            magic_sequence = DEFAULT_MAGIC_SEQUENCE;
        }

        isMagicActive = !magic_sequence.equals("");
    }

    private void showMagicSequenceEditor() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Trigger Sequence (*/+-):");

        // Set up the input
        final EditText input = new EditText(this);

        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(magic_sequence);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                magic_sequence = input.getText().toString();

                isMagicActive = !magic_sequence.equals("");

                prefs.edit().putString("nz.sheehan.calculator.magic_sequence", magic_sequence).apply();
                showMagicNumberEditor();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void showMagicNumberEditor() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Special Number:");

        // Set up the input
        final EditText input = new EditText(this);

        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(String.valueOf(magic_number));
        prefs.edit().putFloat("nz.sheehan.calculator.magic_number", (float)magic_number).apply();
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                magic_number = Long.valueOf(input.getText().toString());
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public void onButtonDecimalLongPress(View view) {
        showMagicSequenceEditor();
    }

    public void onButtonClear(View view) {
        storedValue = 0.0;
        operation = ' ';
        isInDecimal = false;
        wasEqualsLast = false;
        previousOperations = "";

        setInput("0");
        setStoredText("0");
    }

    private CharSequence getInput() {
        return ((TextView)findViewById(R.id.textViewResult)).getText();
    }

    private void setInput(CharSequence text) {
        ((TextView)findViewById(R.id.textViewResult)).setText(text);
    }

    private void setStoredText(CharSequence text) {
        ((TextView)findViewById(R.id.textViewStored)).setText(text);
    }

    private CharSequence getStoredText() {
        return ((TextView)findViewById(R.id.textViewStored)).getText();
    }

    private String nicelyFormatNumberString(double number) {
        if (number % 1 == 0)
            return String.valueOf((long)number);
        else
            return String.valueOf(number);
    }

    private void appendDigit(int digit) {
        CharSequence text = getInput();

        if (text.charAt(0) == '0' && text.length() == 1 || wasEqualsLast)
            setInput(String.valueOf(digit));
        else
            setInput(text + String.valueOf(digit));

        if (wasEqualsLast) {
            setStoredText("0");
            wasEqualsLast = false;
        }
    }

    private void runOperation() {

        double number = Double.valueOf((String)getInput());

        if (isMagicActive) {
            previousOperations += operation;

            if (previousOperations.length() > magic_sequence.length()) {
                previousOperations = previousOperations.substring(1, magic_sequence.length() + 1);
            }
        }

        switch (operation) {
            case '+':
                storedValue = storedValue + number;
                break;
            case '-':
                storedValue = storedValue - number;
                break;
            case '*':
                storedValue = storedValue * number;
                break;
            case '/':
                storedValue = storedValue / number;
                break;
            case ' ':
                storedValue = number;
                break;
        }

        operation = ' ';
        isInDecimal = false;
        wasEqualsLast = false;

        CharSequence leader = getStoredText();
        if (leader.charAt(0) == '0' && leader.length() == 1)
            leader = "";

        setStoredText(leader + " " + nicelyFormatNumberString(number));
        setInput("0");
    }

    public void onButtonPercent(View view) {
        double number = Double.valueOf((String)getInput());
        setInput(String.valueOf(number / 100.0));
    }

    public void onButtonBackspace(View view) {
        CharSequence text = getInput();

        if (text.length() > 1) {
            if (text.charAt(text.length() - 1) == '.')
                isInDecimal = false;

            text = text.subSequence(0, text.length() - 1);
        }
        else {
            if (text.charAt(0) != '0') {
                text = "0";
            }
        }

        setInput(text);
    }

    public void onButtonAdd(View view) {
        runOperation();
        setStoredText(getStoredText() + " +");
        operation = '+';
    }

    public void onButtonSubtract(View view) {
        runOperation();
        setStoredText(getStoredText() + " -");
        operation = '-';
    }

    public void onButtonMultiply(View view) {
        runOperation();
        setStoredText(getStoredText() + " \u00D7");
        operation = '*';
    }

    public void onButtonDivide(View view) {
        runOperation();
        setStoredText(getStoredText() + " \u00F7");
        operation = '/';
    }

    public void onButtonEquals(View view) {
        runOperation();

        setStoredText(getStoredText() + " =");

        if (previousOperations.equals(magic_sequence) && isMagicActive) {
            storedValue = magic_number;
        }

        setInput(nicelyFormatNumberString(storedValue));

        wasEqualsLast = true;
    }

    public void onButtonDecimal(View view) {
        if (!isInDecimal) {
            isInDecimal = true;
            setInput(getInput() + ".");
        }
    }

    public void onButton0(View view) {
        appendDigit(0);
    }

    public void onButton1(View view) {
        appendDigit(1);
    }

    public void onButton2(View view) {
        appendDigit(2);
    }

    public void onButton3(View view) {
        appendDigit(3);
    }

    public void onButton4(View view) {
        appendDigit(4);
    }

    public void onButton5(View view) {
        appendDigit(5);
    }

    public void onButton6(View view) {
        appendDigit(6);
    }

    public void onButton7(View view) {
        appendDigit(7);
    }

    public void onButton8(View view) {
        appendDigit(8);
    }

    public void onButton9(View view) {
        appendDigit(9);
    }
}
