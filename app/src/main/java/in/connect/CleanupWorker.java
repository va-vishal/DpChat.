package in.connect;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import in.connect.Model.Story;

public class CleanupWorker extends Worker {

    public CleanupWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        long currentTime = System.currentTimeMillis();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story");

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot storySnapshot : userSnapshot.getChildren()) {
                        Story story = storySnapshot.getValue(Story.class);
                        if (story != null && currentTime > story.getTimeEnd()) {
                            deleteExpiredStory(userSnapshot.getKey(), storySnapshot.getKey());
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("CleanupWorker", "Database error: " + error.getMessage());
            }
        });

        return Result.success();
    }

    private void deleteExpiredStory(String userId, String storyId) {
        DatabaseReference storyRef = FirebaseDatabase.getInstance().getReference("Story").child(userId).child(storyId);
        storyRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("DeleteStory", "Story " + storyId + " deleted successfully.");
            } else {
                Log.e("DeleteStory", "Failed to delete story " + storyId);
            }
        });
    }
}
