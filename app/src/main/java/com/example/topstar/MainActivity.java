package com.example.topstar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private Game game;
    private Button[] but;
    private ImageView img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        but = new Button[4];

        but[0] = findViewById(R.id.button_main_id1);
        but[1] = findViewById(R.id.button_main_id2);
        but[2] = findViewById(R.id.button_main_id3);
        but[3] = findViewById(R.id.button_main_id4);

        img = findViewById(R.id.image_main_artistimage);

        String htmlDoc = loadData();
        ArrayList<Star> starMap = parseData(htmlDoc);

        game = new Game(starMap);

        instanceState();
    }

    private String loadData() {

        DataLoadInternet data = new DataLoadInternet();
        String htmlDoc = null;

        try {
            htmlDoc = data.execute(getString(R.string.address_site)).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        return htmlDoc;
    }

    private ArrayList<Star> parseData(@NonNull String htmlDoc) {

        Pattern regSearchBlock = Pattern.compile("<div class=\"top10\">(.*?)<div class=\"clear\">");
        Matcher matchBlockTop10 = regSearchBlock.matcher(htmlDoc);

        if (matchBlockTop10.groupCount() != 1) {
            Toast.makeText(this, getString(R.string.error_main_countgroups), Toast.LENGTH_LONG).show();

            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }

        matchBlockTop10.find();
        String htmlDocBlock = htmlDoc.substring(matchBlockTop10.start(), matchBlockTop10.end());

        ArrayList<String> htmlBlockStars = new ArrayList<>();

        Pattern regSearchBlockStars = Pattern.compile("<img src=\"(.*?)\"/>");
        Matcher matchBlockStars = regSearchBlockStars.matcher(htmlDocBlock);

        while (matchBlockStars.find()) {
            htmlBlockStars.add(htmlDocBlock.substring(matchBlockStars.start(), matchBlockStars.end()));
        }

        ArrayList<Star> starMap = new ArrayList<>();

        Pattern regSearchStarLink = Pattern.compile("<img src=\"(.*?)\"");
        Pattern regSearchStarName = Pattern.compile("alt=\"(.*?)\"/>");

        Matcher matchStarLink;
        Matcher matchStarName;

        String starLink = null;
        String starName = null;

        int indexLink;
        int indexName;

        for (String i : htmlBlockStars) {

            matchStarLink = regSearchStarLink.matcher(i);
            matchStarName = regSearchStarName.matcher(i);

            matchStarLink.find();
            matchStarName.find();

            indexLink = startAndEndRegex(matchStarLink);
            indexName = startAndEndRegex(matchStarName);

            starLink = i.substring(matchStarLink.start(indexLink), matchStarLink.end(indexLink));
            starName = i.substring(matchStarName.start(indexName), matchStarName.end(indexName));

            starMap.add(new Star(starName, starLink));
        }

        return starMap;
    }

    private void imageInstance() {
        Star win = game.getWinner();

        ImageLoadInternet imageLoadInternet = new ImageLoadInternet();
        Bitmap bitmap = null;

        try {
            bitmap = imageLoadInternet.execute(win.getImage()).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        assert bitmap != null;
        img.setImageBitmap(bitmap);
    }

    public void onClick(View v) {
        Button button = (Button) v;
        String buttonText = button.getText().toString();

        Toast.makeText(getApplicationContext(),
                game.isWinner(buttonText) ? getString(R.string.text_main_win) : getString(R.string.text_main_lose),
                Toast.LENGTH_SHORT).show();

        instanceState();
    }

    private void instanceState() {
        game.rollStar();
        imageInstance();

        ArrayList<Star> stars = game.getStars();
        Star star = null;

        int butId = 0;

        for (int id : game.getIdes()) {
            star = stars.get(id);
            but[butId].setText(star.getName());
            butId++;
        }
    }

    private int startAndEndRegex(Matcher matcher) {
        return matcher.start(1) != -1 ? 1 : 2;
    }

    private static class ImageLoadInternet extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... strings) {

            InputStream stream = null;
            Bitmap bitmapFactory = null;
            HttpURLConnection connection = null;

            try {
                URL url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                stream = connection.getInputStream();
                bitmapFactory = BitmapFactory.decodeStream(stream);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (stream != null) {
                        stream.close();
                    }
                    if (connection != null) {
                        connection.disconnect();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return bitmapFactory;
        }
    }

    private static class DataLoadInternet extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {

            URL url;
            HttpURLConnection connection = null;
            BufferedReader buffer = null;
            String htmlLine;
            StringBuilder htmlDoc = new StringBuilder();

            try {
                url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream inputStream = connection.getInputStream();
                InputStreamReader reader = new InputStreamReader(inputStream);
                buffer = new BufferedReader(reader);

                while ((htmlLine = buffer.readLine()) != null) {
                    htmlDoc.append(htmlLine);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null)
                    connection.disconnect();

                try {
                    if (buffer != null) {
                        buffer.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return htmlDoc.toString();
        }
    }
}