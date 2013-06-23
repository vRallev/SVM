package net.vrallev.android.svm;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import net.vrallev.android.base.BaseActivity;
import net.vrallev.android.svm.view.CartesianCoordinateSystem;

/**
 * @author Ralf Wondratschek
 */
public class MainActivity extends BaseActivity {

    private CartesianCoordinateSystem mCartesianCoordinateSystem;

    private LabeledPoint.ColorClass mColorClass;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

        mCartesianCoordinateSystem = (CartesianCoordinateSystem) findViewById(R.id.coordinate_system);

        mColorClass = LabeledPoint.ColorClass.RED;
        mCartesianCoordinateSystem.setColorClass(mColorClass);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);

        MenuItem item = menu.findItem(R.id.action_color_class);
        item.setIcon(mColorClass.getDrawable());

        return true;
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_color_class:
                mColorClass = LabeledPoint.ColorClass.getNext(mColorClass);
                item.setIcon(mColorClass.getDrawable());
                mCartesianCoordinateSystem.setColorClass(mColorClass);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
