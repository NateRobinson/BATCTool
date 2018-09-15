package com.arcblock.batctool;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		findViewById(R.id.for_sender_btn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, TxsAsSenderRoleActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("address","16ftSEQ4ctQFDtVZiUBusQUjRrGhM3JYwe");
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});

		findViewById(R.id.for_receiver_btn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, TxsAsReceiverRoleActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("address","16ftSEQ4ctQFDtVZiUBusQUjRrGhM3JYwe");
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
	}
}
