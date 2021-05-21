package com.agiledev.agiledevapp;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileAvatarChangeDialog extends DialogFragment {
    public static String TAG = "ProfileAvatarChangeDialog";
    View view;

    static String username;

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference avatarRef = FirebaseStorage.getInstance().getReference().child("avatars");

    CircleImageView imgAvatar;
    ProgressBar imgAvatarSpinner;
    Bitmap avatarBitmap;
    Button btnAvatarChange, btnSkip, btnConfirm;

    public static ProfileAvatarChangeDialog newInstance(String name) {
        ProfileAvatarChangeDialog fragment = new ProfileAvatarChangeDialog();
        username = name;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.profile_avatar_change_layout, container, false);

        imgAvatar = view.findViewById(R.id.avatar_change_image);
        imgAvatarSpinner = view.findViewById(R.id.avatar_change_spinner);
        btnAvatarChange = view.findViewById(R.id.avatar_change_button);

        btnSkip = view.findViewById(R.id.avatar_change_skip);
        btnConfirm = view.findViewById(R.id.avatar_change_confirm);

        btnAvatarChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeAvatar();
            }
        });

        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmAvatarChange();
            }
        });

        db.collection("UserDetails").document(username)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot doc = task.getResult();
                            if (doc.exists()) {
                                avatarRef.child(username + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Glide.with(getActivity()).load(uri).placeholder(R.drawable.placeholder_med_cast).dontAnimate().listener(new RequestListener<Uri, GlideDrawable>() {
                                            @Override
                                            public boolean onException(Exception e, Uri model, Target<GlideDrawable> target, boolean isFirstResource) {
                                                imgAvatarSpinner.setVisibility(View.GONE);
                                                Toast.makeText(getContext(), "Avatar failed to load!", Toast.LENGTH_SHORT).show();
                                                btnAvatarChange.setEnabled(true);
                                                return false;
                                            }
                                            @Override
                                            public boolean onResourceReady(GlideDrawable resource, Uri model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                                imgAvatarSpinner.setVisibility(View.GONE);
                                                btnAvatarChange.setEnabled(true);
                                                return false;
                                            }
                                        }).into(imgAvatar);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        imgAvatarSpinner.setVisibility(View.GONE);
                                        btnAvatarChange.setEnabled(true);
                                    }
                                });
                            }
                        }
                    }
                });

        return view;
    }

    private static int GET_FROM_GALLERY = 1;

    void changeAvatar() {
        startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        int dataSize = 0;

        if (requestCode == GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            String scheme = uri.getScheme();
            avatarBitmap = null;
            if (scheme.equals(ContentResolver.SCHEME_CONTENT)) {
                try {
                    InputStream fileInputStream = getActivity().getApplicationContext().getContentResolver().openInputStream(uri);
                    dataSize = fileInputStream.available();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (scheme.equals(ContentResolver.SCHEME_FILE)) {
                String path = uri.getPath();
                try {
                    File f = new File(path);
                    dataSize = (int)f.length();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (dataSize / 1024 / 1024 >= 5) {
                Toast.makeText(getActivity(), "You cannot upload an image that is more than 5MB!", Toast.LENGTH_LONG).show();
                return;
            }
            try {
                avatarBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            imgAvatarSpinner.setVisibility(View.VISIBLE);
            Glide.with(getActivity()).load(uri).dontAnimate().listener(new RequestListener<Uri, GlideDrawable>() {
                @Override
                public boolean onException(Exception e, Uri model, Target<GlideDrawable> target, boolean isFirstResource) {
                    imgAvatarSpinner.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Image failed to load!", Toast.LENGTH_SHORT).show();
                    return false;
                }

                @Override
                public boolean onResourceReady(GlideDrawable resource, Uri model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                    imgAvatarSpinner.setVisibility(View.GONE);
                    btnConfirm.setEnabled(true);
                    return false;
                }
            }).into(imgAvatar);
        }
    }

    void confirmAvatarChange() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        avatarBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        final Toast uploadingToast = Toast.makeText(getActivity(), "Image Uploading...", Toast.LENGTH_LONG);

        StorageReference childRef = avatarRef.child(username + ".jpg");
        UploadTask uploadTask = childRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Image upload failed! Make sure the file size is less than 5MB!", Toast.LENGTH_LONG).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                dismiss();
                uploadingToast.cancel();
            }
        });
        uploadingToast.show();
        btnAvatarChange.setEnabled(false);
        btnConfirm.setEnabled(false);
        btnSkip.setEnabled(false);
    }
}
