package net.vrallev.android.svm;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;
import com.google.gson.Gson;
import de.greenrobot.event.EventBus;
import net.vrallev.android.base.BaseActivity;
import net.vrallev.android.svm.gradient.GradientDescent;
import net.vrallev.android.svm.model.ColorClass;
import net.vrallev.android.svm.model.LabeledPoint;
import net.vrallev.android.svm.model.Line;
import net.vrallev.android.svm.model.NormalVector;
import net.vrallev.android.svm.view.CartesianCoordinateSystem;

/**
 * @author Ralf Wondratschek
 */
public class MainActivity extends BaseActivity {

    private CartesianCoordinateSystem mCartesianCoordinateSystem;

    private MenuState mMenuState;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		setContentView(R.layout.activity_main);

        mCartesianCoordinateSystem = (CartesianCoordinateSystem) findViewById(R.id.coordinate_system);

        if (savedInstanceState != null) {
            String json = savedInstanceState.getString(MenuState.class.getName(), null);
            if (json != null) {
                mMenuState = new Gson().fromJson(json, MenuState.class);
            }
        }

        if (mMenuState == null) {
            mMenuState = MenuState.STATE_RED;
        }

        mCartesianCoordinateSystem.setMenuState(mMenuState);
	}

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);

        MenuItem item = menu.findItem(R.id.action_color_class);
        item.setIcon(mMenuState.getDrawable());

        return true;
	}

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean res = super.onPrepareOptionsMenu(menu);

        if (test) {
            menu.findItem(R.id.action_color_class).setVisible(false);
            menu.findItem(R.id.action_test).setVisible(false);
            menu.findItem(R.id.action_test_default).setVisible(false);
        }

        return res;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_color_class:
                mMenuState = MenuState.getNext(mMenuState);
                item.setIcon(mMenuState.getDrawable());
                mCartesianCoordinateSystem.setMenuState(mMenuState);
                return true;

            case R.id.action_test:
                test();
                return true;

            case R.id.action_test_default:
                testDefault();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(MenuState.class.getName(), new Gson().toJson(mMenuState));
    }

    boolean test = false;

    public void onEventMainThread(OptimizerCalculator.ResultEvent event) {
        mCartesianCoordinateSystem.setLine(event.getLine());
        setProgressBarIndeterminateVisibility(false);
        test = false;
        invalidateOptionsMenu();
    }

    private void test() {
        Line line = mCartesianCoordinateSystem.getLine();
        if (line == null) {
            line = new Line(new NormalVector(-1, 1), 0);
            mCartesianCoordinateSystem.setLine(line);
        }

        Optimizer gradientDecent = new GradientDescent(line, mCartesianCoordinateSystem.getPoints());
        OptimizerCalculator.getInstance().calculate(gradientDecent);

        test = true;
        invalidateOptionsMenu();
        setProgressBarIndeterminateVisibility(true);

        Toast.makeText(this, "y = " + Math.round(line.getIncrease() * 100) / 100D + " * x + " + Math.round(line.getOffset() * 100) / 100D, Toast.LENGTH_SHORT).show();
    }

    private void testDefault() {
        mCartesianCoordinateSystem.clearPoints();
        mCartesianCoordinateSystem.addPoint(new LabeledPoint(0.4, 0.4, ColorClass.RED));
        mCartesianCoordinateSystem.addPoint(new LabeledPoint(0.8, 0.6, ColorClass.RED));
        mCartesianCoordinateSystem.addPoint(new LabeledPoint(0.2, 0.6, ColorClass.BLUE));
        mCartesianCoordinateSystem.addPoint(new LabeledPoint(0.4, 1.0, ColorClass.BLUE));

        mCartesianCoordinateSystem.setLine(new Line(0, 0.4, 1, 0.9));

        test();
    }
}
