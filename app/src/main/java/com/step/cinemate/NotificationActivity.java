package com.step.cinemate;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.step.cinemate.Adapters.NotificationAdapter;
import com.step.cinemate.Data.NotificationItem;
import com.step.cinemate.Services.BackendService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class NotificationActivity extends AppCompatActivity implements NotificationAdapter.NotificationClickListener{

    public List<NotificationItem> notifications;
    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notification);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        recyclerView = findViewById(R.id.notificationRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        setNotifications();
    }

    private void setNotifications() {
        notifications = new ArrayList<NotificationItem>();

        BackendService.sendGetRequest(
                getResources().getString(R.string.get_notifications),
                new HashMap<>(),
                response -> {
                    System.out.println("Response: " + "Get notifications");
                    System.out.println("Response Code: " + response.getResponseCode());
                    System.out.println("Response Body: " + response.getResponseBody());

                    int code = response.getResponseCode();
                    if(code == 200) {
                        JSONObject jsonResponse = new JSONObject(response.getResponseBody());
                        JSONArray dataArray = jsonResponse.getJSONArray("notifications");

                        for (int i = 0; i < dataArray.length(); i++) {
                            JSONObject movieJson = dataArray.getJSONObject(i);

                            UUID id = UUID.fromString(movieJson.getString("id"));
                            String text = movieJson.getString("message");
                            String time = movieJson.getString("sentAt");
                            boolean isRead = movieJson.getBoolean("isRead");
                            NotificationItem ni = new NotificationItem(id, text, time, isRead);


                            // Добавляем категорию в список
                            notifications.add(ni);
                        }

                        adapter = new NotificationAdapter(notifications, this);
                        recyclerView.setAdapter(adapter);
                    }
                }
        );
    }

    @Override
    public void onMarkAsReadClicked(UUID id) {
        Map<String, String> paramsPatch = new HashMap<>();
        paramsPatch.put("NotificationId", id.toString());

        Map<String, Boolean> paramsPatchB = new HashMap<>();
        paramsPatchB.put("IsRead", true);

        BackendService.sendPatchRequest(
                getResources().getString(R.string.set_mark_notifications),
                paramsPatch,
                paramsPatchB,
                response -> {
                    System.out.println("Response: " + "Send mark notifications");
                    System.out.println("Response Code: " + response.getResponseCode());
                    System.out.println("Response Body: " + response.getResponseBody());

                    runOnUiThread(() -> {
                        for(NotificationItem ni: notifications) {
                            if (ni.getId().equals(id)) {
                                ni.setIsRead(true);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    });
                }
        );
    }
}