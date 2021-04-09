package com.example.languagetranslation;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.languagetranslation.ml.MyModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

//main activity
public class MainActivity extends AppCompatActivity {

    //variables
    TextView english;
    EditText french;
    Button fTe;
    List french_word_array;
    int[] french_numbers;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //connections made vich ui
        english = (TextView) findViewById(R.id.textViewEnglish);
        french = (EditText) findViewById(R.id.editTextFrench);
        fTe = (Button) findViewById(R.id.fTe);

        //loading json file
        InputStream english_index_is = getResources().openRawResource(R.raw.english_index_word);
        InputStream english_word_is = getResources().openRawResource(R.raw.english_word_index);
        InputStream french_index_is = getResources().openRawResource(R.raw.french_index_word);
        InputStream french_word_is = getResources().openRawResource(R.raw.french_word_index);

        //scanning json file
        Scanner english_index_scanner =  new Scanner(english_index_is);
        Scanner english_word_scanner =  new Scanner(english_word_is);
        Scanner french_index_scanner =  new Scanner(french_index_is);
        Scanner french_word_scanner =  new Scanner(french_word_is);

        //string builder
        StringBuilder english_index_sb  = new StringBuilder();
        StringBuilder english_word_sb  = new StringBuilder();
        StringBuilder french_index_sb  = new StringBuilder();
        StringBuilder french_word_sb  = new StringBuilder();

        while (english_index_scanner.hasNextLine()){
            english_index_sb.append(english_index_scanner.nextLine());
        }
        while (english_word_scanner.hasNextLine()){
            english_word_sb.append(english_word_scanner.nextLine());
        }
        while (french_index_scanner.hasNextLine()){
            french_index_sb.append(french_index_scanner.nextLine());
        }
        while (french_word_scanner.hasNextLine()){
            french_word_sb.append(french_word_scanner.nextLine());
        }

        //arraylist
        french_word_array = new ArrayList();
        french_numbers = new int[16];


        try {
            //parsing json file
            JSONArray jsonArrayFrench = new JSONArray(french_index_sb.toString());
            for(int arr=0; arr<jsonArrayFrench.length();arr++){
                french_word_array.add(jsonArrayFrench.getJSONObject(arr).getString(String.valueOf(arr+1)));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        //button
        fTe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String french_text = french.getText().toString().trim();
                try {

                    //removing punctuation from input
                    String[] words = french_text.replaceAll("[^a-zA-Z ]", "").toLowerCase().split("\\s");
                    for(int q=0;q<words.length;q++){
                        for(int j=0;j<french_word_array.size();j++){
                            if(words[q].equals(french_word_array.get(j))){
                                french_numbers[q] = j+1;
                            }
                        }
                    }
                    //mode loading
                    MyModel model = MyModel.newInstance(MainActivity.this);
                    TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 16}, DataType.FLOAT32);
                    inputFeature0.loadArray(french_numbers);
                    MyModel.Outputs outputs = model.process(inputFeature0);
                    TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
                    List<Integer> output = getMax(outputFeature0.getFloatArray());
                    String show = "";
                    //model output
                    for(int i=0;i<output.size();i++){
                        try {
                            //reading json
                            JSONArray jsonArray = new JSONArray(english_index_sb.toString());

                            if (output.get(i) != 0){
                                //convert number to text
                                show = show + jsonArray.getJSONObject(output.get(i)-1).getString(String.valueOf(output.get(i)))+ " ";

                            }
                            english.setText(show);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }



                    // Releases model resources if no longer used.
                    model.close();
                } catch (IOException e) {
                    // TODO Handle the exception
                }
            }
        });



    }

    //function for maximum value from array
    public List<Integer> getMax(float[] data){
        List<Integer> input= new ArrayList<Integer>();
        for (int i = 0;i<data.length;i=i+200){
            float max = Float.MIN_VALUE;
            int j = 0;
            for(int k=0;k<200;k++){
                if (max < data[i+k]){
                    max = data[i+k];
                    j = i+k;
                }
            }
            input.add(j % 200);
        }
        return input;
    }
}