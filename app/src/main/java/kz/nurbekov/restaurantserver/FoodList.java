package kz.nurbekov.restaurantserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.util.UUID;

import kz.nurbekov.restaurantserver.Common.Common;
import kz.nurbekov.restaurantserver.Interface.ItemClickListener;
import kz.nurbekov.restaurantserver.ViewHolder.FoodViewHolder;
import kz.nurbekov.restaurantserver.model.Category;
import kz.nurbekov.restaurantserver.model.Food;

public class FoodList extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    CoordinatorLayout rootLayout;

    FloatingActionButton fab;

    //Firebase
    FirebaseDatabase database;
    DatabaseReference foodList;
    FirebaseStorage storage;
    StorageReference storageReference;

    String categoruId = "";
    FirebaseRecyclerAdapter<Food, FoodViewHolder> adapter;

    //add new food
    MaterialEditText edName, edtDescription, edtPrice, edtDiscount;
    Button btnSelect, btnUplod;

    Food newFood;

    Uri saveUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);

        //Firebase
        database = FirebaseDatabase.getInstance();
        foodList = database.getReference("Foods");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        //Init
        recyclerView = (RecyclerView) findViewById(R.id.recycler_food);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        rootLayout = (CoordinatorLayout) findViewById(R.id.rootLayout);

        fab = (FloatingActionButton) findViewById(R.id.fab1);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //code late
                showAddFoodDialog();
            }
        });

        if (getIntent() != null) {
            categoruId = getIntent().getStringExtra("CategoryId");
        }
        if (!categoruId.isEmpty()) {
            loadListFood(categoruId);
        }

    }

    private void showAddFoodDialog() {
        //copy code from home activity
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(FoodList.this);
        alertDialog.setTitle("Добавить новое блюдо");
        alertDialog.setMessage("Заполните все поля");

        LayoutInflater inflater = this.getLayoutInflater();
        View add_menu_Layout = inflater.inflate(R.layout.new_food_layout, null);

        edName = add_menu_Layout.findViewById(R.id.edtName);
        edtDescription = add_menu_Layout.findViewById(R.id.edtDescription);
        edtDiscount = add_menu_Layout.findViewById(R.id.edtDiscount);
        edtPrice = add_menu_Layout.findViewById(R.id.edPrice);
        btnSelect = add_menu_Layout.findViewById(R.id.btnSelect);
        btnUplod = add_menu_Layout.findViewById(R.id.btnUpload);

        alertDialog.setView(add_menu_Layout);
        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);

        //event for button
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
                // let user select image from gallery and save url of this image
            }
        });

        btnUplod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });

        //set Button
        alertDialog.setPositiveButton("Принять", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                //here, just create new category
                if (newFood != null) {
                    foodList.push().setValue(newFood);
                    Snackbar.make(rootLayout, "Новая блюдо: " + newFood.getFood() + " добавлена", Snackbar.LENGTH_SHORT)
                            .show();
                }
            }
        });
        alertDialog.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });
        alertDialog.show();
    }

    private void loadListFood(String categoruId) {
        Query query = foodList.orderByChild("menuId").equalTo(categoruId);
        FirebaseRecyclerOptions<Food> options = new FirebaseRecyclerOptions.Builder<Food>().setQuery(query, Food.class).build();
        adapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FoodViewHolder holder, int position, @NonNull Food model) {
                holder.food_name.setText(model.getFood());
                Picasso.get().load(model.getImage()).into(holder.food_image);

                holder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //code late
                    }
                });
            }

            @NonNull
            @Override
            public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.food_item, parent, false);
                return new FoodViewHolder(v);
            }
        };
        adapter.startListening();
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
    }

    private void uploadImage() {
        if (saveUri != null) {
            final ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Загрузка...");
            mDialog.show();

            String imageName = UUID.randomUUID().toString();
            final StorageReference imageFolder = storageReference.child("images/" + imageName);
            imageFolder.putFile(saveUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            mDialog.dismiss();
                            Toast.makeText(FoodList.this, "Загружен!", Toast.LENGTH_SHORT).show();
                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    //set value for new category if image uploaded and we get download link
                                    newFood = new Food();
                                    newFood.setFood(edName.getText().toString());
                                    newFood.setDescription(edtDescription.getText().toString());
                                    newFood.setDiscount(edtDiscount.getText().toString());
                                    newFood.setPrice(edtPrice.getText().toString());
                                    newFood.setMenuId(categoruId);
                                    newFood.setImage(uri.toString());
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    mDialog.dismiss();
                    Toast.makeText(FoodList.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    mDialog.setMessage("Загрузка " + progress + "%");
                }
            });

        }
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), Common.PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Common.PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            saveUri = data.getData();
            btnSelect.setText("Выбрана!");
        }
    }


    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (item.getTitle().equals(Common.UPDATE)) {
            showUpdateFoodDialog(adapter.getRef(item.getOrder()).getKey(), adapter.getItem(item.getOrder()));
        }else if (item.getTitle().equals(Common.DELETE)){
            deleteFood(adapter.getRef(item.getOrder()).getKey());
        }
        return super.onContextItemSelected(item);
    }

    private void deleteFood(String key) {
        foodList.child(key).removeValue();
    }

    private void showUpdateFoodDialog(final String key, final Food item) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(FoodList.this);
        alertDialog.setTitle("Изменить блюда");
        alertDialog.setMessage("Заполните все поля");

        LayoutInflater inflater = this.getLayoutInflater();
        View add_menu_Layout = inflater.inflate(R.layout.new_food_layout, null);

        edName = add_menu_Layout.findViewById(R.id.edtName);
        edtDescription = add_menu_Layout.findViewById(R.id.edtDescription);
        edtDiscount = add_menu_Layout.findViewById(R.id.edtDiscount);
        edtPrice = add_menu_Layout.findViewById(R.id.edPrice);
        btnSelect = add_menu_Layout.findViewById(R.id.btnSelect);
        btnUplod = add_menu_Layout.findViewById(R.id.btnUpload);

        //set default value for view
        edName.setText(item.getFood());
        edtDescription.setText(item.getDescription());
        edtDiscount.setText(item.getDiscount());
        edtPrice.setText(item.getPrice());


        alertDialog.setView(add_menu_Layout);
        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);

        //event for button
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
                // let user select image from gallery and save url of this image
            }
        });

        btnUplod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeImage(item);
            }
        });

        //set Button
        alertDialog.setPositiveButton("Принять", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                //here, just create new category

                    //update information
                    item.setFood(edName.getText().toString());
                    item.setPrice(edtPrice.getText().toString());
                    item.setDiscount(edtDiscount.getText().toString());
                    item.setDescription(edtDescription.getText().toString());

                    foodList.child(key).setValue(item);

                    Snackbar.make(rootLayout, "Блюдо " + item.getFood() + " изменен", Snackbar.LENGTH_SHORT)
                            .show();

            }
        });
        alertDialog.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });
        alertDialog.show();
    }

    private void changeImage(final Food item) {
        if(saveUri != null){
            final ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Загрузка...");
            mDialog.show();

            String imageName = UUID.randomUUID().toString();
            final StorageReference imageFolder = storageReference.child("images/"+imageName);
            imageFolder.putFile(saveUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            mDialog.dismiss();
                            Toast.makeText(FoodList.this, "Загружен!", Toast.LENGTH_SHORT).show();
                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    //set value for new category if image uploaded and we get download link
                                    item.setImage(uri.toString());

                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    mDialog.dismiss();
                    Toast.makeText(FoodList.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    mDialog.setMessage("Загрузка "+progress+"%");
                }
            });

        }
    }
}

