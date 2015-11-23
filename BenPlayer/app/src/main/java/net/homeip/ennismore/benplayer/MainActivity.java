package net.homeip.ennismore.benplayer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    BenUtil mUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mUtil = new BenUtil(this);

    }

    protected void onStart() {
        super.onStart();
        final Button b = (Button) findViewById(R.id.serviceButton);
        if (mUtil.isServerRunning()) {
            b.setText("Stop Background Server");
        } else {
            b.setText("Start Background Server");
        }
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mUtil.isServerRunning()) {
                    mUtil.stopServer();
                    b.setText("Start Background Server");
                } else {
                    mUtil.startServer();
                    b.setText("Stop Background Server");
                }
            }
        });

    }
}
