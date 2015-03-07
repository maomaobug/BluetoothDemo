package zhang.hailing.btdemo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class MainActivity extends ActionBarActivity {
    private Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        spinner = (Spinner) toolbar.findViewById(R.id.spinner);
        initSpinner();
    }

    private void initSpinner() {
        ArrayAdapter<CharSequence> uiAdapter = ArrayAdapter.createFromResource(this,
                R.array.mocked_ui, android.R.layout.simple_spinner_dropdown_item);
        uiAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(uiAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Fragment fragment =
                        position == 0 ? new WearableFragment() : new AppFragment();
                showFragment(fragment);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // no operation
            }
        });
    }

    private void showFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit();
        }
    }

}
