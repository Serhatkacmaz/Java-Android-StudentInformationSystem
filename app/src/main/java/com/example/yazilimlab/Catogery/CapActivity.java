package com.example.yazilimlab.Catogery;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yazilimlab.MainActivity;
import com.example.yazilimlab.Model.CustomDialog;
import com.example.yazilimlab.Model.UsersData;
import com.example.yazilimlab.R;
import com.example.yazilimlab.RegisterActivity;
import com.example.yazilimlab.StudentHomeActivity;
import com.example.yazilimlab.StudentHomeFragment.MakeApplicationFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLOutput;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class CapActivity extends AppCompatActivity {

    // ComboBox for Ogretim turu
    private AutoCompleteTextView educationCapTypeDropDown, educationCapTypePassDropDown;
    ArrayList<String> arrayListCapEducationType;
    ArrayAdapter<String> arrayAdapterCapEducationType;

    // combo box for InfoClass
    private AutoCompleteTextView facultyDropDown, departmentDropDown;
    ArrayList<String> arrayListFaculty, arrayListDepartment;
    ArrayAdapter<String> arrayAdapterFaculty, arrayAdapterDepartment;

    //Firebase
    private FirebaseAuth fAuth;
    private FirebaseUser fUser;
    private FirebaseFirestore firebaseFirestore;
    private DocumentReference docRef;
    FirebaseStorage storage;
    StorageReference storageReference;

    // ArrayList
    private ArrayList<Uri> fileUriList;
    private ArrayList<String> fileType;

    // hashMap
    private HashMap<String, String> resourcesAdd;

    // path
    private String transcriptPath, petitionPath;

    // Uri
    private Uri transcriptUri, pdfUri, petitionUri;

    // image file state
    private ImageView image_cap_fileStateTranscript;
    private TextView textView_cap_fileStateTranscript;

    // str
    private String strEducationCapType, strEducationCapTypePass, strCapFaculty, strCapBranch;

    // code
    private static final int CREATE_PDF = 1;
    private static final int PICK_FILE = 1;

    // flag for activityResult
    private boolean flagPdf, flagFileTranscript;

    // incoming data
    private UsersData usersData;

    // progress dialog
    private CustomDialog customDialog;

    // init
    private void init() {
        // ComboBox for Ogretim turu
        educationCapTypeDropDown = (AutoCompleteTextView) findViewById(R.id.autoCompleteCapEducationType);
        educationCapTypePassDropDown = (AutoCompleteTextView) findViewById(R.id.autoCompleteCapEducationTypePass);
        arrayListCapEducationType = new ArrayList<>();
        arrayListCapEducationType.add("I.Öğretim");
        arrayListCapEducationType.add("II.Öğretim");
        arrayAdapterCapEducationType = new ArrayAdapter<String>(getApplicationContext(), R.layout.support_simple_spinner_dropdown_item, arrayListCapEducationType);
        educationCapTypeDropDown.setAdapter(arrayAdapterCapEducationType);
        educationCapTypePassDropDown.setAdapter(arrayAdapterCapEducationType);

        facultyDropDown = (AutoCompleteTextView) findViewById(R.id.autoCompleteCapFaculty);
        departmentDropDown = (AutoCompleteTextView) findViewById(R.id.autoCompleteCapDep);

        //arrayList
        fileUriList = new ArrayList<Uri>();
        fileType = new ArrayList<String>();
        fileType.add("Transcript/");
        fileType.add("Petition/");


        //Firebase
        fAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        usersData = new UsersData();

        // file state
        image_cap_fileStateTranscript = (ImageView) findViewById(R.id.image_cap_fileStateTranscript);
        textView_cap_fileStateTranscript = (TextView) findViewById(R.id.textView_cap_fileStateTranscript);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cap);
        init();

        getDataComboBoxFaculty();
        // Fakulte ismi degistiğinde
        facultyDropDown.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                getDataComboBoxDepartment();
            }
        });

    }

    // fakulte isimlerini getir
    private void getDataComboBoxFaculty() {
        arrayListFaculty = new ArrayList<>();
        firebaseFirestore.collection("Faculties")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // System.out.println(document.getId() + " => " + document.getData());
                                arrayListFaculty.add(document.getId());
                            }
                        } else {

                        }
                    }
                });

        arrayAdapterFaculty = new ArrayAdapter<>(getApplicationContext(), R.layout.support_simple_spinner_dropdown_item, arrayListFaculty);
        facultyDropDown.setAdapter(arrayAdapterFaculty);
    }

    // fakulteye gore bolum isimleri
    private void getDataComboBoxDepartment() {
        arrayListDepartment = new ArrayList<>();
        docRef = firebaseFirestore.collection("Faculties").document(facultyDropDown.getText().toString());
        docRef.get().addOnSuccessListener(CapActivity.this, new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    //System.out.println(documentSnapshot.getData().values().size());
                    for (int i = 0; i < documentSnapshot.getData().values().size(); i++) {
                        //System.out.println(documentSnapshot.getData().get(String.valueOf(i)));
                        arrayListDepartment.add(String.valueOf(documentSnapshot.getData().get(String.valueOf(i))));
                    }
                }
            }
        });

        arrayAdapterDepartment = new ArrayAdapter<>(getApplicationContext(), R.layout.support_simple_spinner_dropdown_item, arrayListDepartment);
        departmentDropDown.setAdapter(arrayAdapterDepartment);
    }

    //string degerleri atama
    private void setString() {
        strEducationCapType = educationCapTypeDropDown.getText().toString();
        strEducationCapTypePass = educationCapTypePassDropDown.getText().toString();
        strCapFaculty = facultyDropDown.getText().toString();
        strCapBranch = departmentDropDown.getText().toString();
    }

    // input bos kontrolu
    private boolean isNotEmptyStrings() {
        setString();
        boolean result = TextUtils.isEmpty(strEducationCapType) || TextUtils.isEmpty(strCapFaculty) || TextUtils.isEmpty(strCapBranch) || TextUtils.isEmpty(strEducationCapTypePass);

        if (result)
            return false;
        return true;
    }

    //->pdf Start

    //https://github.com/LukeDaniel16/CreatePDFwithJavaOnAndroidStudio
    public void initPdf(String title) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        flagPdf = true;
        flagFileTranscript = false;
        intent.putExtra(Intent.EXTRA_TITLE, title);
        startActivityForResult(intent, CREATE_PDF);
    }

    //pdf içeriği
    private void createPdf(Uri uri) {
        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();
        paint.setLinearText(true);
        paint.setTypeface(Typeface.MONOSPACE);
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(210, 297, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(4);
        paint.setFakeBoldText(false);

        // aktif tarih
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String strDate = formatter.format(date);

        canvas.drawText(strDate, 190, 12, paint);
        paint.setFakeBoldText(true);
        canvas.drawText("T.C.", pageInfo.getPageWidth() / 2, 20, paint);
        canvas.drawText("KOCAELİ ÜNİVERSİTESİ", pageInfo.getPageWidth() / 2, 26, paint);
        canvas.drawText(usersData.getIncomingFaculty().toUpperCase(), pageInfo.getPageWidth() / 2, 32, paint);
        canvas.drawText(usersData.getIncomingDepartment().toUpperCase() + " BÖLÜM BAŞKANLIĞINA", pageInfo.getPageWidth() / 2, 38, paint);

        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(3);
        paint.setFakeBoldText(false);

        TextPaint mTextPaint = new TextPaint();
        mTextPaint.setTypeface(Typeface.MONOSPACE);
        mTextPaint.setTextSize(3);
        String mText = "\t" + usersData.getIncomingFaculty() + " "
                + usersData.getIncomingDepartment() + " Bölümü "
                + strEducationCapType + " " + usersData.getIncomingNumber()
                + " numaralı " + usersData.getIncomingName() + " "
                + usersData.getIncomingLastName() + " isimli öğrencisiyim."
                + "\n\tKocaeli Üniversitesi Ön Lisans ve Lisans Eğitim ve Öğretim Yönetmeliği’nin 45.maddesi uyarınca, "
                + strCapFaculty + "nde " + strCapBranch + " Bölümü'nde  "
                + strEducationCapTypePass + " Çift Anadal Programı (ÇAP) kapsamında öğrenim görme talebimin kabul edilmesini arz ederim.";


        StaticLayout mTextLayout = new StaticLayout(mText, mTextPaint, canvas.getWidth() - 60, Layout.Alignment.ALIGN_NORMAL, 1, 1, true);

        canvas.save();
// calculate x and y position where your text will be placed
        int textX = 30;
        int textY = 45;
        canvas.translate(textX, textY);
        mTextLayout.draw(canvas);
        canvas.restore();


        canvas.drawText("İmza:", 135, 80, paint);
        //İmza Kutusu

        //sol
        canvas.drawLine(150, 75, 150, 85, paint);
        //sağ
        canvas.drawLine(180, 75, 180, 85, paint);
        //yukarı
        canvas.drawLine(150, 75, 180, 75, paint);
        //aşağı
        canvas.drawLine(150, 85, 180, 85, paint);


        //table1 row
        canvas.drawLine(30, 90, 180, 90, paint);
        canvas.drawLine(30, 95, 180, 95, paint);
        canvas.drawLine(30, 100, 180, 100, paint);
        canvas.drawLine(30, 105, 180, 105, paint);
        canvas.drawLine(30, 110, 180, 110, paint);
        canvas.drawLine(30, 115, 180, 115, paint);
        canvas.drawLine(30, 130, 180, 130, paint);
        //table1 context
        canvas.drawText("Adı ve Soyadı", 33, 94, paint);
        canvas.drawText(usersData.getIncomingName() + " " + usersData.getIncomingLastName(), 85, 94, paint);
        canvas.drawText("Öğrenci No", 33, 99, paint);
        canvas.drawText(usersData.getIncomingNumber(), 85, 99, paint);
        canvas.drawText("Bölümü-Sınıfı", 33, 104, paint);
        canvas.drawText(usersData.getIncomingDepartment() + " - " + usersData.getIncomingClass(), 85, 104, paint);
        canvas.drawText("Cep Telefon No ", 33, 109, paint);
        canvas.drawText(usersData.getIncomingPhone(), 85, 109, paint);
        canvas.drawText("E-posta Adresi", 33, 114, paint);
        canvas.drawText(usersData.getIncomingMail(), 85, 114, paint);
        canvas.drawText("Adresi", 33, 119, paint);


        TextPaint mAdressTextPaint = new TextPaint();
        mAdressTextPaint.setTypeface(Typeface.MONOSPACE);
        mAdressTextPaint.setTextSize(3);
        String mAdressText = usersData.getIncomingAddress();


        StaticLayout mAdressTextLayout = new StaticLayout(mAdressText, mAdressTextPaint, canvas.getWidth() - 120, Layout.Alignment.ALIGN_NORMAL, 1, 1, true);

        canvas.save();
// calculate x and y position where your text will be placed
        int textAX = 85;
        int textAY = 118;
        canvas.translate(textAX, textAY);
        mAdressTextLayout.draw(canvas);
        canvas.restore();


        //table1 column
        canvas.drawLine(30, 90, 30, 130, paint);
        canvas.drawLine(80, 90, 80, 130, paint);  // orta
        canvas.drawLine(180, 90, 180, 130, paint);

        paint.setFakeBoldText(true);
        canvas.drawText("Ekler", 33, 140, paint);
        paint.setFakeBoldText(false);
        canvas.drawText("EK-1\tTranskript ", 35, 144, paint);

        paint.setFakeBoldText(true);
        canvas.drawText("Çift Anadal Programı", 30, 155, paint);
        paint.setFakeBoldText(false);
        paint.setTextSize(2);
        canvas.drawText("MADDE 43 – (1) Bir bölümün öğrencileri, ön lisans/lisans öğrenimleri boyunca aynı fakülte, yüksekokul ve meslek", 30, 158, paint);
        canvas.drawText("yüksekokulu içinde veya dışında asıl bölümüne konu bakımından yakın olan başka bir lisans öğretimini aynı zamanda takip", 30, 161, paint);
        canvas.drawText("edebilir. Bununla ilgili esaslar Senato tarafından belirlenir.", 30, 164, paint);

        canvas.drawText("MADDE 6- (2) Başvuru anında anadal diploma programındaki GNO'su 4.00'lük not sisteminde en az 3.00 olan ve anadal", 30, 170, paint);
        canvas.drawText("başvurabilir.", 30, 173, paint);

        canvas.drawText("c) Anadal diploma programındaki GNO'su en az 3.00 olan ancak anadal diploma programının ilgili sınıfında başarı sıralaması", 30, 177, paint);
        canvas.drawText("itibariyle en üst %20'sinde yer almayan öğrencilerden çift anadal yapılacak bölümün/programın ilgili yıldaki taban puanından az", 30, 180, paint);
        canvas.drawText("olmamak üzere puana sahip olanlar da ÇAP'a başvurabilirler.", 30, 183, paint);

        pdfDocument.finishPage(page);
        setPdf(uri, pdfDocument);
    }

    // pdf kaydet
    private void setPdf(Uri uri, PdfDocument pdfDocument) {
        try {
            BufferedOutputStream stream = new BufferedOutputStream(Objects.requireNonNull(getContentResolver().openOutputStream(uri)));
            pdfDocument.writeTo(stream);
            pdfDocument.close();
            stream.flush();
            petitionUri = uri;

        } catch (FileNotFoundException e) {
            Toast.makeText(this, "Dosya hatası bulunamadı.", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(this, "Giriş ve çıkış hatası.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Bilinmeyen hata." + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    // https://github.com/LukeDaniel16/CreatePDFwithJavaOnAndroidStudio

    //->pdf End


    // transkript belge sec buton
    public void selectFileTranscript(View view) {
        flagPdf = false;
        flagFileTranscript = true;
        Intent intent = new Intent();
        //https://stackoverflow.com/questions/1698050/multiple-mime-types-in-android
        intent.setType("*/*");
        String[] mimetypes = {"application/msword", "application/pdf", "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
        //https://stackoverflow.com/questions/1698050/multiple-mime-types-in-android
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Dosya Seç"), PICK_FILE);
    }


    //https://stackoverflow.com/questions/9758151/get-the-file-extension-from-images-picked-from-gallery-or-camera-as-string
    public String getMimeType(Context context, Uri uri) {
        //seçilen dosya uzantısı tespit etme
        String extension;
        //Check uri format to avoid null
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            //If scheme is a content
            final MimeTypeMap mime = MimeTypeMap.getSingleton();
            extension = mime.getExtensionFromMimeType(context.getContentResolver().getType(uri));
        } else {
            //If scheme is a File
            //This will replace white spaces with %20 and also other special characters. This will avoid returning null values on file name with spaces and special characters.
            extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(uri.getPath())).toString());
        }
        return extension;
    }
    //https://stackoverflow.com/questions/9758151/get-the-file-extension-from-images-picked-from-gallery-or-camera-as-string


    // dosya kayıt format isimlendirme
    private String adjustFormat() {
        String number, name, lastName;
        number = usersData.getIncomingNumber();
        name = usersData.getIncomingName();
        lastName = usersData.getIncomingLastName();

        // https://www.javatpoint.com/java-get-current-date
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyyHHmm");
        String strDate = formatter.format(date);
        // https://www.javatpoint.com/java-get-current-date

        return number + "_" + name + "_" + lastName + "_" + strDate;
    }


    // basvurular firebase save
    private void saveResources() {

        // https://www.javatpoint.com/java-get-current-date
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String strDate = formatter.format(date);
        // https://www.javatpoint.com/java-get-current-date

        fUser = fAuth.getCurrentUser();
        resourcesAdd = new HashMap<String, String>();
        resourcesAdd.put("type", "Çap");
        resourcesAdd.put("userUid", fUser.getUid());
        resourcesAdd.put("state", "0");
        resourcesAdd.put("transcriptPath", transcriptPath);
        resourcesAdd.put("petitionPath", petitionPath);
        resourcesAdd.put("studentNumber", usersData.getIncomingNumber());
        resourcesAdd.put("date", strDate);

        firebaseFirestore.collection("Resources").document()
                .set(resourcesAdd).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(CapActivity.this, "Başvurunuz yapıldı.\nİmzalı belgeyi yükleyin.", Toast.LENGTH_LONG).show();
                System.out.println("Cap basvuru kayıt tamam");
                customDialog.dismissDialog();
                startActivity(new Intent(CapActivity.this, StudentHomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });
    }


    // transkript firebase save
    private void saveTranscriptFileInStorage() {

        fileUriList.add(1, petitionUri);
        System.out.println();
        if (fileUriList.size() == 2) {

            for (int i = 0; i < fileUriList.size(); i++) {
                String extension = getMimeType(CapActivity.this, fileUriList.get(i));

                if (i == 0) {
                    transcriptPath = "CAP/" + extension + "/" + fileType.get(i) + adjustFormat();
                } else if (i == 1) {
                    petitionPath = "CAP/" + extension + "/" + fileType.get(i) + adjustFormat();
                }

                StorageReference reference = storageReference.child("CAP").child(extension).child(fileType.get(i) + adjustFormat());

                reference.putFile(fileUriList.get(i)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // path alma
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isComplete()) ;
                        String linkUri = uriTask.getResult().getPath();
                        System.out.println("Uri: " + String.valueOf(linkUri));
                        System.out.println("Cap dosya Kayıt Tamam.");
                        System.out.println("----------------------");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(CapActivity.this, "Dosyalar sisteme yüklenirken hata oluştu.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }


    // onayla butonu
    public void submitCAP(View view) {
        if (isNotEmptyStrings() && transcriptUri != null) {
            AlertDialog.Builder checkAlertDialog = new AlertDialog.Builder(CapActivity.this);
            checkAlertDialog.setTitle("Onaylama");
            checkAlertDialog.setMessage("Başvurunuzu tamamlamak istiyor musunuz?");
            checkAlertDialog.setPositiveButton("Evet", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    initPdf("CapBasvurusu");    // pdf
                }
            });
            checkAlertDialog.setNegativeButton("Hayır", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    System.out.println("Hayır Bastın");
                }
            });
            checkAlertDialog.create().show();
        } else {
            Toast.makeText(CapActivity.this, "Boş alanlar var.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean t1 = false;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // pdf
        if (requestCode == CREATE_PDF && resultCode == RESULT_OK && data.getData() != null && flagPdf) {

            customDialog = new CustomDialog(CapActivity.this);
            customDialog.startLoadingDialog();

            pdfUri = data.getData();
            createPdf(pdfUri);
            saveTranscriptFileInStorage();
            saveResources();
        } else if (requestCode == PICK_FILE && resultCode == RESULT_OK && data != null && data.getData() != null && flagFileTranscript) {
            // dosya transkiript
            transcriptUri = data.getData();

            if (fileUriList.size() > 0 && t1) {
                fileUriList.remove(0);
                fileUriList.add(0, transcriptUri);
            } else {
                fileUriList.add(0, transcriptUri);
                t1 = true;
            }

            //System.out.println(getMimeType(CapActivity.this,transcriptUri));
            image_cap_fileStateTranscript.setImageResource(R.drawable.yes);
            textView_cap_fileStateTranscript.setText("Transkript Dosyasını Değiştir");
        }
    }
}