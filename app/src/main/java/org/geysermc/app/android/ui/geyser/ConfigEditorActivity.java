/*
 * Copyright (c) 2020-2020 GeyserMC. http://geysermc.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * @author GeyserMC
 * @link https://github.com/GeyserMC/GeyserAndroid
 */

package org.geysermc.app.android.ui.geyser;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import org.geysermc.app.android.R;
import org.geysermc.app.android.utils.AndroidUtils;
import org.geysermc.connector.common.serializer.AsteriskSerializer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ConfigEditorActivity extends AppCompatActivity {

    private EditText txtConfig;
    private String configText = "Unable to locate config, please start the server first!";
    private File configFile;
    private boolean showRaw;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the users editor preference
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        showRaw = preferences.getString("geyser_config_editor", "pretty").equals("raw");

        // Fetch the stored config file
        configFile = AndroidUtils.getStoragePath(getApplicationContext()).resolve("config.yml").toFile();

        if (showRaw) {
            setContentView(R.layout.activity_config_editor_raw);

            // Load the config
            txtConfig = findViewById(R.id.txtConfig);

            if (configFile.exists()) {
                // Enable horizontal scrolling
                txtConfig.setHorizontallyScrolling(true);

                // Get the config file text
                configText = AndroidUtils.fileToString(configFile);
                txtConfig.setText(configText);
            } else {
                txtConfig.setText(configText);
                txtConfig.setEnabled(false);
            }

            // Hide the loader
            AndroidUtils.HideLoader();
        } else {
            // Show the pretty editor
            setContentView(R.layout.activity_config_editor_pretty);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new ConfigEditorFragment())
                    .commit();
        }

        // Enable the back button
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // The back button
            case android.R.id.home:
                if (checkForChanges()) {
                    super.onBackPressed();
                }
                return true;
            case R.id.action_config_help:
                AndroidUtils.showURL("https://github.com/GeyserMC/Geyser/wiki/Understanding-the-Config");
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (checkForChanges()) {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.config, menu);
        return true;
    }

    private boolean checkForChanges() {
        if (showRaw) {
            // Check if they have changed any values
            if (!configText.equals(txtConfig.getText().toString())) {
                AlertDialog confirmDialog = new AlertDialog.Builder(this).create();
                confirmDialog.setTitle("Save");
                confirmDialog.setMessage("Do you wish to save the config?");
                confirmDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Save", (dialog, id) -> {
                    try {
                        FileWriter configWriter = new FileWriter(configFile);
                        configWriter.write(txtConfig.getText().toString());
                        configWriter.close();
                        this.finish();
                    } catch (IOException e) {
                        AndroidUtils.showToast(getApplicationContext(), "Unable to write config!");
                    }
                });

                confirmDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Discard", (dialog, id) -> {
                    this.finish();
                });

                confirmDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Cancel", (dialog, id) -> {
                    // Do nothing
                });

                confirmDialog.show();

                return false;
            } else {
                return true;
            }
        } else if (ConfigEditorFragment.isConfigChanged()) {
            AlertDialog confirmDialog = new AlertDialog.Builder(this).create();
            confirmDialog.setTitle("Save");
            confirmDialog.setMessage("Do you wish to save the config?");
            confirmDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Save", (dialog, id) -> {
                try {
                    AsteriskSerializer.showSensitive = true;

                    // Build and write the updated config yml
                    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
                    mapper.writeValue(configFile, ConfigEditorFragment.getConfiguration());

                    AsteriskSerializer.showSensitive = false;

                    this.finish();
                } catch (IOException e) {
                    AndroidUtils.showToast(getApplicationContext(), "Unable to write config!");
                }
            });

            confirmDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Discard", (dialog, id) -> {
                this.finish();
            });

            confirmDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Cancel", (dialog, id) -> {
                // Do nothing
            });

            confirmDialog.show();

            return false;
        } else {
            return true;
        }
    }
}