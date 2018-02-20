package info.androidhive.rxjavaretrofit.view;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.util.List;

import info.androidhive.rxjavaretrofit.R;
import info.androidhive.rxjavaretrofit.network.ApiClient;
import info.androidhive.rxjavaretrofit.network.ApiService;
import info.androidhive.rxjavaretrofit.network.model.Note;
import info.androidhive.rxjavaretrofit.network.model.User;
import info.androidhive.rxjavaretrofit.utils.PrefUtils;
import io.reactivex.CompletableObserver;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private ApiService apiService;
    private CompositeDisposable disposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // white background notification bar
        whiteNotificationBar(fab);


        apiService = ApiClient.getClient(getApplicationContext()).create(ApiService.class);


        if (TextUtils.isEmpty(PrefUtils.getApiKey(this))) {
            testApi();
        }

        createNote("Hello from android device!");

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
                                for (Note note : notes) {
                                    Log.e(TAG, "Note: " + note.getId() + ", " + note.getNote() + ", " + note.getTimestamp());
                                }
                            }

                            @Override
                            public void onError(Throwable e) {

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
