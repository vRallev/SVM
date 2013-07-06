package net.vrallev.android.svm;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import android.widget.Toast;
import com.google.gson.Gson;
import com.manuelpeinado.refreshactionitem.ProgressIndicatorType;
import com.manuelpeinado.refreshactionitem.RefreshActionItem;
import de.greenrobot.event.EventBus;
import net.vrallev.android.base.BaseActivity;
import net.vrallev.android.svm.model.DirtyLineEvent;
import net.vrallev.android.svm.method.GradientDescent;
import net.vrallev.android.svm.method.NewtonMethod;
import net.vrallev.android.svm.method.SubGradientDescent;
import net.vrallev.android.svm.model.ColorClass;
import net.vrallev.android.svm.model.LabeledPoint;
import net.vrallev.android.svm.model.Line;
import net.vrallev.android.svm.model.NormalVector;
import net.vrallev.android.svm.view.CartesianCoordinateSystem;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ralf Wondratschek
 */
@SuppressWarnings("UnusedDeclaration")
public class MainActivity extends BaseActivity {

    private static final String NAVIGATION_POSITION = "navigationPosition";
    private static final String COORDINATE_SYSTEM_STATE = "coordinateSystemState";

    private CartesianCoordinateSystem mCartesianCoordinateSystem;
    private CartesianCoordinateSystem.State mCoordinateSystemState;

    private int mNavigationPosition;
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

            json = savedInstanceState.getString(COORDINATE_SYSTEM_STATE, null);
            if (json != null) {
                mCoordinateSystemState = new Gson().fromJson(json, CartesianCoordinateSystem.State.class);
            }
        }

        if (mMenuState == null) {
            mMenuState = MenuState.STATE_RED;
        }

        mCartesianCoordinateSystem.setMenuState(mMenuState);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(false);

        SpinnerAdapter spinnerAdapter = ArrayAdapter.createFromResource(actionBar.getThemedContext(), R.array.action_list, android.R.layout.simple_spinner_dropdown_item);

        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        actionBar.setListNavigationCallbacks(spinnerAdapter, new ActionBar.OnNavigationListener() {
            @Override
            public boolean onNavigationItemSelected(int itemPosition, long itemId) {
                mNavigationPosition = itemPosition;
                return true;
            }
        });

        if (savedInstanceState != null) {
            actionBar.setSelectedNavigationItem(savedInstanceState.getInt(NAVIGATION_POSITION, 1));
        } else {
            actionBar.setSelectedNavigationItem(1);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().registerSticky(this, OptimizerCalculator.ResultEvent.class);
        EventBus.getDefault().register(this, DirtyLineEvent.class);
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
                calculate();
            }
        });

        if (OptimizerCalculator.getInstance().isCalculating()) {
            mRefreshActionItem.showProgress(true);
        }

        if (mCoordinateSystemState == null) {
            mCoordinateSystemState = mCartesianCoordinateSystem.getState();
        }

        if (EventBus.getDefault().getStickyEvent(DirtyLineEvent.class) != null) {
            onEventMainThread((DirtyLineEvent) EventBus.getDefault().getStickyEvent(DirtyLineEvent.class));
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

            case R.id.action_clear_points:
                mCartesianCoordinateSystem.clearPoints();
                return true;

            case R.id.action_insert_default:
                insertDefault();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(MenuState.class.getName(), new Gson().toJson(mMenuState));
        outState.putInt(NAVIGATION_POSITION, getActionBar().getSelectedNavigationIndex());
        outState.putString(COORDINATE_SYSTEM_STATE, new Gson().toJson(mCoordinateSystemState));
    }

    public void onEventMainThread(OptimizerCalculator.ResultEvent event) {
        mCartesianCoordinateSystem.setLine(event.getLine(), true);
        mRefreshActionItem.hideBadge();
        mRefreshActionItem.showProgress(false);

        mCoordinateSystemState.releasePoints();
        mCoordinateSystemState = mCartesianCoordinateSystem.getState();

        EventBus.getDefault().removeStickyEvent(event);
        EventBus.getDefault().removeStickyEvent(DirtyLineEvent.class);

        if (event.isDiverged()) {
            new AlertDialog.Builder(new ContextThemeWrapper(this, android.R.style.Theme_Holo_Dialog))
                    .setCancelable(true)
                    .setMessage("You have chosen a bad start value. The method diverged.\n\nThe starting line was reset.")
                    .setTitle("Your fault")
                    .setPositiveButton(android.R.string.ok, null)
                    .setIcon(R.drawable.ic_launcher)
                    .show();
        }
    }

    public void onEventMainThread(DirtyLineEvent event) {
        CartesianCoordinateSystem.State state = mCartesianCoordinateSystem.getState();
        if (!state.equals(mCoordinateSystemState)) {
            if (!mRefreshActionItem.isBadgeVisible()) {
                mRefreshActionItem.showBadge();
            }
        } else {
            if (mRefreshActionItem.isBadgeVisible()) {
                mRefreshActionItem.hideBadge();
            }
        }
        state.releasePoints();
    }

    private void calculate() {
        if (!LabeledPoint.hasColorClass(mCartesianCoordinateSystem.getPoints(), ColorClass.RED) || !LabeledPoint.hasColorClass(mCartesianCoordinateSystem.getPoints(), ColorClass.BLUE)) {
            Toast.makeText(this, "You need to insert at least one point for each class.", Toast.LENGTH_LONG).show();
            return;
        }

        mRefreshActionItem.showProgress(true);
        mRefreshActionItem.hideBadge();

        Line line = mCartesianCoordinateSystem.getLine();
        if (line == null) {
            line = new Line(new NormalVector(-1, 1), 0);
            mCartesianCoordinateSystem.setLine(line, false);
        }

        line.getNormalVector().setW1(line.getNormalVector().getW1() / line.getNormalVector().getW2());
        line.getNormalVector().setW2(1);

        Optimizer optimizer;
        switch (mNavigationPosition) {
            case 0:
                optimizer = new SubGradientDescent(mCartesianCoordinateSystem.getLine(), mCartesianCoordinateSystem.getPoints());
                break;
            case 1:
                optimizer = new GradientDescent(mCartesianCoordinateSystem.getLine(), mCartesianCoordinateSystem.getPoints());
                break;
            case 2:
                optimizer = new NewtonMethod(mCartesianCoordinateSystem.getLine(), mCartesianCoordinateSystem.getPoints());
                break;
            default:
                optimizer = null;
                break;
        }

        OptimizerCalculator.getInstance().calculate(optimizer);

        mRefreshActionItem.showProgress(true);
    }

    private void insertDefault() {
        List<LabeledPoint> points = mCartesianCoordinateSystem.getPoints();
        List<LabeledPoint> toAdd = new ArrayList<LabeledPoint>();
        toAdd.add(LabeledPoint.getInstance(0.4, 0.4, ColorClass.RED));
        toAdd.add(LabeledPoint.getInstance(0.7, 0.6, ColorClass.RED));
        toAdd.add(LabeledPoint.getInstance(0.2, 0.6, ColorClass.BLUE));
        toAdd.add(LabeledPoint.getInstance(0.4, 0.9, ColorClass.BLUE));

        if (!LabeledPoint.listEqual(points, toAdd)) {
            mCartesianCoordinateSystem.clearPoints();
            for (LabeledPoint p : toAdd) {
                mCartesianCoordinateSystem.addPoint(p);
            }
        } else {
            for (LabeledPoint p : toAdd) {
                p.release();
            }
        }

        Line line = new Line(0, 0.4, 1, 0.9);
        if (!line.equals(mCartesianCoordinateSystem.getLine())) {
            mCartesianCoordinateSystem.setLine(line, true);
        }
    }
}
