package com.example.pdm2_projeto;

import android.app.AlertDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
        editTextName = view.findViewById(R.id.editText_name);
        editTextEmail = view.findViewById(R.id.editText_email);
        changePassword = view.findViewById(R.id.change_password);
        deleteAccountButton = view.findViewById(R.id.delete_account_button);
        deleteAccountWarning = view.findViewById(R.id.delete_account_warning);
        accountCreationDate = view.findViewById(R.id.account_creation_date);

        // Configure the back button
        configureBackButton(view);

        // Ensure UI elements are correctly referenced before updating them
        updateLocalizedTexts(view);

        // Load user data from Firebase Firestore
        loadUserData();

        // Handle profile image editing
        if (profilePicture != null) {
            profilePicture.setOnClickListener(v -> showProfilePictureDialog());
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

    private void configureBackButton(View view) {
        view.findViewById(R.id.back_button).setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
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

                    // Carregar imagem de perfil
                    if (user.getProfilePictureUrl() != null && !user.getProfilePictureUrl().isEmpty()) {
                        Glide.with(requireContext())
                                .load(user.getProfilePictureUrl())
                                .into(profilePicture);
                    }

                    // Obter e formatar a data de criação da conta
                    long creationTimestamp = currentUser.getMetadata().getCreationTimestamp();
                    String formattedDate = formatTimestamp(creationTimestamp);

                    if (accountCreationDate != null) {
                        accountCreationDate.setText(getString(R.string.account_creation_date) + " " + formattedDate);
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), getString(R.string.failed_to_fetch_locations), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(new Date(timestamp));
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
    private void showProfilePictureDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        // Criando um TextView personalizado para o título
        TextView title = new TextView(requireContext());
        title.setText(getString(R.string.change_profile_picture));
        title.setTextSize(26);
        title.setGravity(Gravity.CENTER);
        title.setPadding(20, 70, 20, 50);
        title.setTextColor(getResources().getColor(R.color.app_primary_text));

        builder.setCustomTitle(title);

        // Opções do menu
        String[] options = {
                getString(R.string.upload_picture),
                getString(R.string.remove_current_picture),
                getString(R.string.cancel)
        };

        // Criar um adaptador personalizado
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, options) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                // Criar layout do item
                LinearLayout layout = new LinearLayout(requireContext());
                layout.setOrientation(LinearLayout.VERTICAL);

                    View divider = new View(requireContext());
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, 2); // Altura do divisor (2dp)
                    params.setMargins(0, 0, 0, 0); // Margem lateral
                    divider.setLayoutParams(params);
                    divider.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                    layout.addView(divider);

                // Criar o TextView para exibir a opção
                TextView textView = new TextView(requireContext());
                textView.setText(getItem(position));
                textView.setGravity(Gravity.CENTER);
                textView.setTextSize(16);
                textView.setPadding(0, 40, 0, 40); // Adiciona espaçamento interno

                // Define as cores dos botões
                if (position == 0) { // "Carregar Foto" (Verde)
                    textView.setTextColor(getResources().getColor(R.color.app_primary_text));
                    textView.setTypeface(Typeface.DEFAULT_BOLD);
                } else if (position == 1) { // "Remover Foto Atual" (Vermelho)
                    textView.setTextColor(getResources().getColor(R.color.red));
                    textView.setTypeface(Typeface.DEFAULT_BOLD);
                } else { // "Cancelar"
                    textView.setTextColor(getResources().getColor(R.color.app_primary_text));
                }

                // Adiciona o texto ao layout
                layout.addView(textView);

                return layout;
            }
        };

        builder.setAdapter(adapter, (dialog, which) -> {
            switch (which) {
                case 0: // Carregar foto
                    openImagePicker();
                    break;
                case 1: // Remover foto atual
                    showRemoveProfilePictureDialog();
                    break;
                case 2: // Cancelar
                    dialog.dismiss();
                    break;
            }
        });

        // Criar e mostrar o AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();

        // Definir fundo arredondado após o show()
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(900, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_rounded_corners);
        }
    }

    private void openImagePicker() {
        Toast.makeText(getContext(), "Abrir galeria (ainda não implementado)", Toast.LENGTH_SHORT).show();
    }

    private void showRemoveProfilePictureDialog() {
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.remove_profile_picture))
                .setMessage(getString(R.string.remove_profile_picture_message))
                .setPositiveButton(getString(R.string.yes), null) // Define o botão, mas sem ação inicial
                .setNegativeButton(getString(R.string.no), null)  // Define o botão, mas sem ação inicial
                .create();

        dialog.setOnShowListener(d -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

            // Define a cor verde para o "Sim"
            positiveButton.setTextColor(getResources().getColor(R.color.app_green));
            positiveButton.setTypeface(Typeface.DEFAULT_BOLD);
            positiveButton.setOnClickListener(v -> {
                removeProfilePicture();
                dialog.dismiss();
            });

            // Define a cor vermelha para o "Não"
            negativeButton.setTextColor(getResources().getColor(R.color.red));
            negativeButton.setTypeface(Typeface.DEFAULT_BOLD);
            negativeButton.setOnClickListener(v -> dialog.dismiss());
        });

        // Criar e mostrar o AlertDialog
        dialog.show();

        // Definir fundo arredondado após o show()
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(900, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_rounded_corners);
        }
    }

    private void removeProfilePicture() {
        if (profilePicture != null) {
            profilePicture.setImageResource(R.drawable.ic_profile); // Define uma imagem padrão
        }

        usersRepository.updateProfilePicture(null, new FirestoreCallback() {
            @Override
            public void onSuccess(Object result) {
                Toast.makeText(getContext(), getString(R.string.profile_picture_removed), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), getString(R.string.profile_picture_remove_failed) + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
        layout.setPadding(0, 20, 0, 20);

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
