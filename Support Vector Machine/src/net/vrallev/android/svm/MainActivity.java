package net.vrallev.android.svm;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import net.vrallev.android.base.BaseActivity;
import net.vrallev.android.base.util.L;
import net.vrallev.android.svm.view.CartesianCoordinateSystem;

import java.util.ArrayList;
import java.util.List;

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
//                mColorClass = LabeledPoint.ColorClass.getNext(mColorClass);
//                item.setIcon(mColorClass.getDrawable());
//                mCartesianCoordinateSystem.setColorClass(mColorClass);
                test();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void test() {
        List<LabeledPoint> points = new ArrayList<LabeledPoint>();
        points.add(new LabeledPoint(2, 2, LabeledPoint.ColorClass.BLUE));
        points.add(new LabeledPoint(4, 3, LabeledPoint.ColorClass.BLUE));
        points.add(new LabeledPoint(1, 3, LabeledPoint.ColorClass.RED));
        points.add(new LabeledPoint(2, 5, LabeledPoint.ColorClass.RED));

        Line line = new Line(0, 2, 4, 4);

        GradientDescent gradientDescent = new GradientDescent(line, points);
        Line line1 = gradientDescent.calc(2);
        L.debug("line1 " + line1.getIncrease() + " " + line1.getOffset());

    }
}
