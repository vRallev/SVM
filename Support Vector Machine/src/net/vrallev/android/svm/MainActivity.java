package net.vrallev.android.svm;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import com.google.gson.Gson;
import com.manuelpeinado.refreshactionitem.ProgressIndicatorType;
import com.manuelpeinado.refreshactionitem.RefreshActionItem;
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
@SuppressWarnings("UnusedDeclaration")
public class MainActivity extends BaseActivity {

    private CartesianCoordinateSystem mCartesianCoordinateSystem;

    private MenuState mMenuState;

    private RefreshActionItem mRefreshActionItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        EventBus.getDefault().registerSticky(this);
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

        item = menu.findItem(R.id.action_refresh);
        mRefreshActionItem = (RefreshActionItem) item.getActionView();
        mRefreshActionItem.setMenuItem(item);
        mRefreshActionItem.setProgressIndicatorType(ProgressIndicatorType.INDETERMINATE);
        mRefreshActionItem.setRefreshActionListener(new RefreshActionItem.RefreshActionListener() {
            @Override
            public void onRefreshButtonClick(RefreshActionItem sender) {
//                mRefreshActionItem.showProgress(true);
                mRefreshActionItem.hideBadge();
            }
        });

        if (OptimizerCalculator.getInstance().isCalculating()) {
            mRefreshActionItem.showProgress(true);
        }

        return true;
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

    public void onEventMainThread(OptimizerCalculator.ResultEvent event) {
        mCartesianCoordinateSystem.setLine(event.getLine());
        mRefreshActionItem.showProgress(false);

        EventBus.getDefault().removeStickyEvent(event);
    }

    private void test() {
        Line line = mCartesianCoordinateSystem.getLine();
        if (line == null) {
            line = new Line(new NormalVector(-1, 1), 0);
            mCartesianCoordinateSystem.setLine(line);
        }

        Optimizer gradientDecent = new GradientDescent(line, mCartesianCoordinateSystem.getPoints());
        OptimizerCalculator.getInstance().calculate(gradientDecent);

        mRefreshActionItem.showProgress(true);
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
