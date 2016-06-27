package su.swage.recovery;

import android.content.Intent;
import android.os.Bundle;

import com.github.paolorotolo.appintro.AppIntro2;

public class WelcomeIntro extends AppIntro2 {
    @Override
    public void init(Bundle savedInstanceState) {
        addSlide(WNewFragment.newInstance(R.layout.intro1));
        addSlide(WNewFragment.newInstance(R.layout.intro2));
        addSlide(WNewFragment.newInstance(R.layout.intro3));
        //addSlide(WNewFragment.newInstance(R.layout.intro4));
    }

    @Override
    public void onNextPressed() {
    }

    @Override
    public void onDonePressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onSlideChanged() {
    }
}