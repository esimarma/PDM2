package com.example.pdm2_projeto;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.pdm2_projeto.interfaces.FirestoreCallback;
import com.example.pdm2_projeto.models.User;
import com.example.pdm2_projeto.repositories.UsersRepository;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AccountFragment extends Fragment {

    private ImageView profilePicture, editProfilePicture;
    private EditText editTextName, editTextEmail;
    private TextView changePassword, accountCreationDate, deleteAccountWarning;
    private Button deleteAccountButton;
    private UsersRepository usersRepository;
    private FirebaseAuth auth;

    public AccountFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        // Initialize Firebase Authentication and Firestore Repository
        auth = FirebaseAuth.getInstance();
        usersRepository = new UsersRepository();

        // Bind UI elements to Java variables
        profilePicture = view.findViewById(R.id.profile_picture);
        editProfilePicture = view.findViewById(R.id.edit_profile_image);
        editTextName = view.findViewById(R.id.editText_name);
        editTextEmail = view.findViewById(R.id.editText_email);
        changePassword = view.findViewById(R.id.change_password);
        deleteAccountButton = view.findViewById(R.id.delete_account_button);
        deleteAccountWarning = view.findViewById(R.id.delete_account_warning);
        accountCreationDate = view.findViewById(R.id.account_creation_date);

        // Ensure UI elements are correctly referenced before updating them
        updateLocalizedTexts(view);

        // Load user data from Firebase Firestore
        loadUserData();

        // Handle profile image editing
        if (editProfilePicture != null) {
            editProfilePicture.setOnClickListener(v -> openImagePicker());
        }

        // Handle password reset using a dialog
        if (changePassword != null) {
            changePassword.setOnClickListener(v -> showPasswordResetDialog());
        }

        // Handle account deletion confirmation
        if (deleteAccountButton != null) {
            deleteAccountButton.setOnClickListener(v -> confirmAccountDeletion());
        }

        return view;
    }

    /**
     * Updates all UI texts to reflect the currently selected language.
     */
    private void updateLocalizedTexts(View view) {
        TextView nameField = view.findViewById(R.id.name_field);
        TextView emailField = view.findViewById(R.id.email_field);

        if (nameField != null) nameField.setText(getString(R.string.name));
        if (emailField != null) emailField.setText(getString(R.string.email));
        if (changePassword != null) changePassword.setText(getString(R.string.change_password));
        if (deleteAccountButton != null) deleteAccountButton.setText(getString(R.string.delete_account));
            accountCreationDate.setText(getString(R.string.account_creation_date) + " #data");
            deleteAccountWarning.setText(getString(R.string.delete_account_warning));

    }

    /**
     * Fetches the logged-in user's data from Firestore and displays it.
     */
    private void loadUserData() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), getString(R.string.not_signed_in), Toast.LENGTH_SHORT).show();
            return;
        }

        usersRepository.getCurrentUser(new FirestoreCallback() {
            @Override
            public void onSuccess(Object result) {
                User user = (User) result;
                if (user != null) {
                    if (editTextName != null) editTextName.setText(user.getName());
                    if (editTextEmail != null) editTextEmail.setText(currentUser.getEmail());

                    // Load profile picture (if available)
                    if (user.getProfilePictureUrl() != null && !user.getProfilePictureUrl().isEmpty() && profilePicture != null) {
                        Glide.with(requireContext())
                                .load(user.getProfilePictureUrl())
                                .into(profilePicture);
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), getString(R.string.failed_to_fetch_locations), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Shows a dialog where the user can request a password reset.
     */
    private void showPasswordResetDialog() {
        EditText resetEmail = new EditText(requireContext());
        resetEmail.setHint(getString(R.string.reset_email_hint));

        if (editTextEmail != null) {
            resetEmail.setText(editTextEmail.getText().toString());
        }

        AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(requireContext());
        passwordResetDialog.setTitle(getString(R.string.reset_password_title));
        passwordResetDialog.setMessage(getString(R.string.reset_password_message));
        passwordResetDialog.setView(resetEmail);

        passwordResetDialog.setPositiveButton(getString(R.string.send), (dialog, which) -> {
            String email = resetEmail.getText().toString().trim();
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(requireContext(), getString(R.string.enter_valid_email), Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnSuccessListener(aVoid -> Toast.makeText(requireContext(), getString(R.string.reset_email_sent), Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(requireContext(), getString(R.string.reset_email_failed) + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        passwordResetDialog.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss());
        passwordResetDialog.show();
    }

    /**
     * Opens an image picker for profile picture editing.
     */
    private void openImagePicker() {
        Toast.makeText(getContext(), "Feature not implemented yet!", Toast.LENGTH_SHORT).show();
    }

    /**
     * Shows a confirmation dialog for account deletion.
     */
    private void confirmAccountDeletion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(getString(R.string.delete_account_title));
        builder.setMessage(getString(R.string.delete_account_message));

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(20, 20, 20, 20);

        EditText passwordInput = new EditText(requireContext());
        passwordInput.setHint(getString(R.string.enter_password));
        passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(passwordInput);

        builder.setView(layout);

        builder.setPositiveButton(getString(R.string.confirm), (dialog, which) -> {
            String password = passwordInput.getText().toString().trim();
            if (password.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.enter_valid_password), Toast.LENGTH_SHORT).show();
                return;
            }
            reauthenticateAndDeleteAccount(password);
        });

        builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss());

        builder.show();
    }


/**
     * Reauthenticates the user before deleting the account.
     */
    private void reauthenticateAndDeleteAccount(String password) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), getString(R.string.not_signed_in), Toast.LENGTH_SHORT).show();
            return;
        }

        String email = currentUser.getEmail();
        if (email == null) {
            Toast.makeText(getContext(), getString(R.string.enter_valid_email), Toast.LENGTH_SHORT).show();
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(email, password);

        currentUser.reauthenticate(credential)
                .addOnSuccessListener(authResult -> {
                    usersRepository.deleteUser(currentUser.getUid(), new FirestoreCallback() {
                        @Override
                        public void onSuccess(Object result) {
                            currentUser.delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(getContext(), getString(R.string.account_deleted), Toast.LENGTH_SHORT).show();
                                        requireActivity().getSupportFragmentManager()
                                                .beginTransaction()
                                                .replace(R.id.fragment_container, new LoginFragment())
                                                .commit();
                                    });
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(getContext(), getString(R.string.account_delete_failed), Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                            requireActivity().getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.fragment_container, new LoginFragment())
                                    .commit();
                        }
                    });
                });
    }
}
