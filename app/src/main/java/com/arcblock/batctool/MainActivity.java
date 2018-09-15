package com.arcblock.batctool;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

	private EditText address_input_et;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		address_input_et = findViewById(R.id.address_input_et);

		findViewById(R.id.for_sender_btn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (checkCanCommit()) {
					Intent intent = new Intent(MainActivity.this, TxsAsSenderRoleActivity.class);
					Bundle bundle = new Bundle();
					bundle.putString("address", address_input_et.getText().toString().trim());
					intent.putExtras(bundle);
					startActivity(intent);
				}
			}
		});

		findViewById(R.id.for_receiver_btn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (checkCanCommit()) {
					Intent intent = new Intent(MainActivity.this, TxsAsReceiverRoleActivity.class);
					Bundle bundle = new Bundle();
					bundle.putString("address", address_input_et.getText().toString().trim());
					intent.putExtras(bundle);
					startActivity(intent);
				}
			}
		});
	}

	private boolean checkCanCommit() {
		if (TextUtils.isEmpty(address_input_et.getText().toString().trim())) {
			Toast.makeText(this, "请输入Address地址", Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}
}
