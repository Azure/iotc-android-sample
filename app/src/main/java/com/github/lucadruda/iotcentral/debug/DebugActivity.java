package com.github.lucadruda.iotcentral.debug;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Spinner;

import com.github.lucadruda.iotcentral.IoTCentral;
import com.github.lucadruda.iotcentral.R;
import com.github.lucadruda.iotcentral.adapters.MeasureAdapter;
import com.github.lucadruda.iotcentral.service.templates.DevKitTemplate;
import com.github.lucadruda.iotcentral.service.types.Measure;

import java.util.ArrayList;
import java.util.List;

public class DebugActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitvity_debug);
        List<Measure> measures = IoTCentral.getMeasures("130772c7-97dd-4a76-bbdb-9209888293f6");
        measures.add(0, new Measure(MeasureAdapter.DEFAULT_TEXT_KEY, "Select telemetry...", Measure.MeasureType.TELEMETRY));
        for (int i = 1; i <= 6; i++) {
            Spinner spinner = findViewById(getResources().getIdentifier("spinner" + i, "id", getPackageName()));
            MeasureAdapter adapter = new MeasureAdapter(this, android.R.id.text1, new ArrayList<Measure>(measures), "spinner" + i);
            spinner.setAdapter(adapter);
            spinner.setOnItemSelectedListener(adapter.getOnItemSelectedListener());
        }
    }
}
