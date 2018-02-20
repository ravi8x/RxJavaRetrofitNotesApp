package info.androidhive.rxjavaretrofit.view;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import info.androidhive.rxjavaretrofit.R;
import info.androidhive.rxjavaretrofit.network.ApiClient;
import info.androidhive.rxjavaretrofit.network.ApiService;
import info.androidhive.rxjavaretrofit.network.model.Note;
import info.androidhive.rxjavaretrofit.network.model.User;
import info.androidhive.rxjavaretrofit.utils.MyDividerItemDecoration;
import info.androidhive.rxjavaretrofit.utils.PrefUtils;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private ApiService apiService;
    private CompositeDisposable disposable = new CompositeDisposable();
    private NotesAdapter mAdapter;
    private List<Note> notesList = new ArrayList<>();

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, NewNoteActivity.class));
            }
        });

        // white background notification bar
        whiteNotificationBar(fab);

        mAdapter = new NotesAdapter(this, notesList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new MyDividerItemDecoration(this, LinearLayoutManager.VERTICAL, 16));
        recyclerView.setAdapter(mAdapter);

        apiService = ApiClient.getClient(getApplicationContext()).create(ApiService.class);


        if (TextUtils.isEmpty(PrefUtils.getApiKey(this))) {
            testApi();
        }

//        createNote("Call brother!");
//        createNote("Complete RxJava series");
//        createNote("Buy milk today");
//        createNote("Bank Details: HDFC Bank, Acc: 2018382990003, IFSC Code: HDFC23920");
//        createNote("Watch Black Panther!");
//        createNote("My email: ravi8x@gmail.com");
//        createNote("Imsai Arasan 23am Pulikesi!");

        fetchAllNotes();

        updateNote(24, "Note updated from device!");

        deleteNote(26);
    }

    private void deleteNote(final int noteId) {
        disposable.add(
                apiService.deleteNote(noteId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableCompletableObserver() {
                            @Override
                            public void onComplete() {
                                Log.e(TAG, "Note deleted! " + noteId);
                            }

                            @Override
                            public void onError(Throwable e) {

                            }
                        })
        );
    }

    private void updateNote(int noteId, String note) {
        disposable.add(
                apiService.updateNote(noteId, note)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableCompletableObserver() {
                            @Override
                            public void onComplete() {
                                Log.e(TAG, "Note updated!");
                            }

                            @Override
                            public void onError(Throwable e) {

                            }
                        }));
    }

    private void fetchAllNotes() {
        disposable.add(
                apiService.fetchAllNotes()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<List<Note>>() {
                            @Override
                            public void onSuccess(List<Note> notes) {

                                // TODO - remove loop
                                for (Note note : notes) {
                                    Log.e(TAG, "Note: " + note.getId() + ", " + note.getNote() + ", " + note.getTimestamp());
                                }

                                notesList.clear();
                                notesList.addAll(notes);
                                mAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onError(Throwable e) {
                                // TODO - handle all error cases
                            }
                        })
        );
    }

    private void createNote(String note) {
        disposable.add(
                apiService.createNote(note)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<Note>() {

                            @Override
                            public void onSuccess(Note note) {
                                Log.e(TAG, "new note created: " + note.getId() + ", " + note.getNote() + ", " + note.getTimestamp());
                            }

                            @Override
                            public void onError(Throwable e) {

                            }
                        }));
    }

    private void testApi() {
        // register user
        disposable.add(
                apiService
                        .register("genymotion")
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<User>() {
                            @Override
                            public void onSuccess(User user) {
                                PrefUtils.storeApiKey(getApplicationContext(), user.getApiKey());

                                Log.e(TAG, "ApiKey: " + PrefUtils.getApiKey(getApplicationContext()));
                            }

                            @Override
                            public void onError(Throwable e) {

                            }
                        }));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void whiteNotificationBar(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = view.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            view.setSystemUiVisibility(flags);
            getWindow().setStatusBarColor(Color.WHITE);
        }
    }
}
