package at.htl_villach.chatapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import at.htl_villach.chatapplication.bll.Chat;
import at.htl_villach.chatapplication.bll.User;
import at.htl_villach.chatapplication.fragments.ChatroomFragment;
import de.hdodenhof.circleimageview.CircleImageView;


public class SingleChatActivity extends AppCompatActivity {

    private Chat currentChat;
    private User selectedUser;

    FirebaseUser fuser;
    DatabaseReference referenceUsers;
    StorageReference storageReference;
    private final long MAX_DOWNLOAD_IMAGE = 1024 * 1024 * 5;

    //toolbar
    Toolbar toolbar;
    TextView toolbarTitle;
    CircleImageView toolbarPicture;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_chat);

        Intent intent = getIntent();
        currentChat = (Chat) intent.getParcelableExtra("selectedChat");

        storageReference = FirebaseStorage.getInstance().getReference();

        fuser = FirebaseAuth.getInstance().getCurrentUser();
        referenceUsers = FirebaseDatabase.getInstance().getReference("Users").child(currentChat.getReceiver(fuser.getUid()));
        referenceUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                selectedUser = snapshot.getValue(User.class);

                //set Layouts with user data
                toolbarTitle.setText(selectedUser.getFullname());
                toolbarPicture.post(new Runnable() {
                    @Override
                    public void run() {
                        storageReference.child(selectedUser.getId() + "/profilePicture.jpg").getBytes(MAX_DOWNLOAD_IMAGE)
                                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                    @Override
                                    public void onSuccess(byte[] bytes) {
                                        selectedUser.setProfilePictureResource(bytes);
                                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                        toolbarPicture.setImageBitmap(Bitmap.createScaledBitmap(bitmap, toolbarPicture.getWidth(),
                                                toolbarPicture.getHeight(), false));
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        toolbarPicture.setImageResource(R.drawable.standard_picture);
                                    }
                                });
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        toolbar = (Toolbar) findViewById(R.id.toolbar_chat);
        toolbarPicture = (CircleImageView) findViewById(R.id.toolbar_profilpicture);
        toolbarTitle = (TextView) findViewById(R.id.toolbar_title);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_acion_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //startActivity(new Intent(SingleChatActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                finish();
            }
        });

        //toDo: loadProfilPicture
        //toolbarPicture.setImageResource();

        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SingleChatActivity.this, ProfileActivity.class);
                intent.putExtra("selectedContact", selectedUser);
                startActivity(intent);
            }
        });

        ChatroomFragment chatroom = (ChatroomFragment) getFragmentManager().findFragmentById(R.id.chatroom);

        chatroom = ChatroomFragment.newInstance(currentChat);
        getFragmentManager().beginTransaction().add(R.id.chatroom, chatroom).commit();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuChatProfil:
                Intent intent = new Intent(SingleChatActivity.this, ProfileActivity.class);
                intent.putExtra("selectedContact", selectedUser);
                startActivity(intent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(SingleChatActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }
}
