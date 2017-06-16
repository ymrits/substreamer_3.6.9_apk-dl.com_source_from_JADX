package android.support.v4.app;

import android.graphics.Rect;
import android.support.annotation.RequiresApi;
import android.transition.Transition;
import android.transition.Transition.EpicenterCallback;
import android.transition.Transition.TransitionListener;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@RequiresApi(21)
class FragmentTransitionCompat21 {

    /* renamed from: android.support.v4.app.FragmentTransitionCompat21.1 */
    static class C00101 extends EpicenterCallback {
        final /* synthetic */ Rect val$epicenter;

        C00101(Rect rect) {
            this.val$epicenter = rect;
        }

        public Rect onGetEpicenter(Transition transition) {
            return this.val$epicenter;
        }
    }

    /* renamed from: android.support.v4.app.FragmentTransitionCompat21.2 */
    static class C00112 implements TransitionListener {
        final /* synthetic */ ArrayList val$exitingViews;
        final /* synthetic */ View val$fragmentView;

        C00112(View view, ArrayList arrayList) {
            this.val$fragmentView = view;
            this.val$exitingViews = arrayList;
        }

        public void onTransitionStart(Transition transition) {
        }

        public void onTransitionEnd(Transition transition) {
            transition.removeListener(this);
            this.val$fragmentView.setVisibility(8);
            int numViews = this.val$exitingViews.size();
            for (int i = 0; i < numViews; i++) {
                ((View) this.val$exitingViews.get(i)).setVisibility(0);
            }
        }

        public void onTransitionCancel(Transition transition) {
        }

        public void onTransitionPause(Transition transition) {
        }

        public void onTransitionResume(Transition transition) {
        }
    }

    /* renamed from: android.support.v4.app.FragmentTransitionCompat21.3 */
    static class C00123 implements Runnable {
        final /* synthetic */ ArrayList val$inNames;
        final /* synthetic */ int val$numSharedElements;
        final /* synthetic */ ArrayList val$outNames;
        final /* synthetic */ ArrayList val$sharedElementsIn;
        final /* synthetic */ ArrayList val$sharedElementsOut;

        C00123(int i, ArrayList arrayList, ArrayList arrayList2, ArrayList arrayList3, ArrayList arrayList4) {
            this.val$numSharedElements = i;
            this.val$sharedElementsIn = arrayList;
            this.val$inNames = arrayList2;
            this.val$sharedElementsOut = arrayList3;
            this.val$outNames = arrayList4;
        }

        public void run() {
            for (int i = 0; i < this.val$numSharedElements; i++) {
                ((View) this.val$sharedElementsIn.get(i)).setTransitionName((String) this.val$inNames.get(i));
                ((View) this.val$sharedElementsOut.get(i)).setTransitionName((String) this.val$outNames.get(i));
            }
        }
    }

    /* renamed from: android.support.v4.app.FragmentTransitionCompat21.4 */
    static class C00134 implements Runnable {
        final /* synthetic */ Map val$nameOverrides;
        final /* synthetic */ ArrayList val$sharedElementsIn;

        C00134(ArrayList arrayList, Map map) {
            this.val$sharedElementsIn = arrayList;
            this.val$nameOverrides = map;
        }

        public void run() {
            int numSharedElements = this.val$sharedElementsIn.size();
            for (int i = 0; i < numSharedElements; i++) {
                View view = (View) this.val$sharedElementsIn.get(i);
                String name = view.getTransitionName();
                if (name != null) {
                    view.setTransitionName(FragmentTransitionCompat21.findKeyForValue(this.val$nameOverrides, name));
                }
            }
        }
    }

    /* renamed from: android.support.v4.app.FragmentTransitionCompat21.5 */
    static class C00145 implements TransitionListener {
        final /* synthetic */ Object val$enterTransition;
        final /* synthetic */ ArrayList val$enteringViews;
        final /* synthetic */ Object val$exitTransition;
        final /* synthetic */ ArrayList val$exitingViews;
        final /* synthetic */ Object val$sharedElementTransition;
        final /* synthetic */ ArrayList val$sharedElementsIn;

        C00145(Object obj, ArrayList arrayList, Object obj2, ArrayList arrayList2, Object obj3, ArrayList arrayList3) {
            this.val$enterTransition = obj;
            this.val$enteringViews = arrayList;
            this.val$exitTransition = obj2;
            this.val$exitingViews = arrayList2;
            this.val$sharedElementTransition = obj3;
            this.val$sharedElementsIn = arrayList3;
        }

        public void onTransitionStart(Transition transition) {
            if (this.val$enterTransition != null) {
                FragmentTransitionCompat21.replaceTargets(this.val$enterTransition, this.val$enteringViews, null);
            }
            if (this.val$exitTransition != null) {
                FragmentTransitionCompat21.replaceTargets(this.val$exitTransition, this.val$exitingViews, null);
            }
            if (this.val$sharedElementTransition != null) {
                FragmentTransitionCompat21.replaceTargets(this.val$sharedElementTransition, this.val$sharedElementsIn, null);
            }
        }

        public void onTransitionEnd(Transition transition) {
        }

        public void onTransitionCancel(Transition transition) {
        }

        public void onTransitionPause(Transition transition) {
        }

        public void onTransitionResume(Transition transition) {
        }
    }

    /* renamed from: android.support.v4.app.FragmentTransitionCompat21.6 */
    static class C00156 extends EpicenterCallback {
        final /* synthetic */ Rect val$epicenter;

        C00156(Rect rect) {
            this.val$epicenter = rect;
        }

        public Rect onGetEpicenter(Transition transition) {
            if (this.val$epicenter == null || this.val$epicenter.isEmpty()) {
                return null;
            }
            return this.val$epicenter;
        }
    }

    /* renamed from: android.support.v4.app.FragmentTransitionCompat21.7 */
    static class C00167 implements Runnable {
        final /* synthetic */ Map val$nameOverrides;
        final /* synthetic */ ArrayList val$sharedElementsIn;

        C00167(ArrayList arrayList, Map map) {
            this.val$sharedElementsIn = arrayList;
            this.val$nameOverrides = map;
        }

        public void run() {
            int numSharedElements = this.val$sharedElementsIn.size();
            for (int i = 0; i < numSharedElements; i++) {
                View view = (View) this.val$sharedElementsIn.get(i);
                view.setTransitionName((String) this.val$nameOverrides.get(view.getTransitionName()));
            }
        }
    }

    FragmentTransitionCompat21() {
    }

    public static Object cloneTransition(Object transition) {
        if (transition != null) {
            return ((Transition) transition).clone();
        }
        return null;
    }

    public static Object wrapTransitionInSet(Object transition) {
        if (transition == null) {
            return null;
        }
        Object transitionSet = new TransitionSet();
        transitionSet.addTransition((Transition) transition);
        return transitionSet;
    }

    public static void setSharedElementTargets(Object transitionObj, View nonExistentView, ArrayList<View> sharedViews) {
        TransitionSet transition = (TransitionSet) transitionObj;
        List<View> views = transition.getTargets();
        views.clear();
        int count = sharedViews.size();
        for (int i = 0; i < count; i++) {
            bfsAddViewChildren(views, (View) sharedViews.get(i));
        }
        views.add(nonExistentView);
        sharedViews.add(nonExistentView);
        addTargets(transition, sharedViews);
    }

    private static void bfsAddViewChildren(List<View> views, View startView) {
        int startIndex = views.size();
        if (!containedBeforeIndex(views, startView, startIndex)) {
            views.add(startView);
            for (int index = startIndex; index < views.size(); index++) {
                View view = (View) views.get(index);
                if (view instanceof ViewGroup) {
                    ViewGroup viewGroup = (ViewGroup) view;
                    int childCount = viewGroup.getChildCount();
                    for (int childIndex = 0; childIndex < childCount; childIndex++) {
                        View child = viewGroup.getChildAt(childIndex);
                        if (!containedBeforeIndex(views, child, startIndex)) {
                            views.add(child);
                        }
                    }
                }
            }
        }
    }

    private static boolean containedBeforeIndex(List<View> views, View view, int maxIndex) {
        for (int i = 0; i < maxIndex; i++) {
            if (views.get(i) == view) {
                return true;
            }
        }
        return false;
    }

    public static void setEpicenter(Object transitionObj, View view) {
        if (view != null) {
            Transition transition = (Transition) transitionObj;
            Rect epicenter = new Rect();
            getBoundsOnScreen(view, epicenter);
            transition.setEpicenterCallback(new C00101(epicenter));
        }
    }

    public static void getBoundsOnScreen(View view, Rect epicenter) {
        int[] loc = new int[2];
        view.getLocationOnScreen(loc);
        epicenter.set(loc[0], loc[1], loc[0] + view.getWidth(), loc[1] + view.getHeight());
    }

    public static void addTargets(Object transitionObj, ArrayList<View> views) {
        Transition transition = (Transition) transitionObj;
        if (transition != null) {
            int i;
            if (transition instanceof TransitionSet) {
                TransitionSet set = (TransitionSet) transition;
                int numTransitions = set.getTransitionCount();
                for (i = 0; i < numTransitions; i++) {
                    addTargets(set.getTransitionAt(i), views);
                }
            } else if (!hasSimpleTarget(transition) && isNullOrEmpty(transition.getTargets())) {
                int numViews = views.size();
                for (i = 0; i < numViews; i++) {
                    transition.addTarget((View) views.get(i));
                }
            }
        }
    }

    private static boolean hasSimpleTarget(Transition transition) {
        return (isNullOrEmpty(transition.getTargetIds()) && isNullOrEmpty(transition.getTargetNames()) && isNullOrEmpty(transition.getTargetTypes())) ? false : true;
    }

    private static boolean isNullOrEmpty(List list) {
        return list == null || list.isEmpty();
    }

    public static Object mergeTransitionsTogether(Object transition1, Object transition2, Object transition3) {
        TransitionSet transitionSet = new TransitionSet();
        if (transition1 != null) {
            transitionSet.addTransition((Transition) transition1);
        }
        if (transition2 != null) {
            transitionSet.addTransition((Transition) transition2);
        }
        if (transition3 != null) {
            transitionSet.addTransition((Transition) transition3);
        }
        return transitionSet;
    }

    public static void scheduleHideFragmentView(Object exitTransitionObj, View fragmentView, ArrayList<View> exitingViews) {
        ((Transition) exitTransitionObj).addListener(new C00112(fragmentView, exitingViews));
    }

    public static Object mergeTransitionsInSequence(Object exitTransitionObj, Object enterTransitionObj, Object sharedElementTransitionObj) {
        Transition staggered = null;
        Transition exitTransition = (Transition) exitTransitionObj;
        Transition enterTransition = (Transition) enterTransitionObj;
        Transition sharedElementTransition = (Transition) sharedElementTransitionObj;
        if (exitTransition != null && enterTransition != null) {
            staggered = new TransitionSet().addTransition(exitTransition).addTransition(enterTransition).setOrdering(1);
        } else if (exitTransition != null) {
            staggered = exitTransition;
        } else if (enterTransition != null) {
            staggered = enterTransition;
        }
        if (sharedElementTransition == null) {
            return staggered;
        }
        TransitionSet together = new TransitionSet();
        if (staggered != null) {
            together.addTransition(staggered);
        }
        together.addTransition(sharedElementTransition);
        return together;
    }

    public static void beginDelayedTransition(ViewGroup sceneRoot, Object transition) {
        TransitionManager.beginDelayedTransition(sceneRoot, (Transition) transition);
    }

    public static ArrayList<String> prepareSetNameOverridesOptimized(ArrayList<View> sharedElementsIn) {
        ArrayList<String> names = new ArrayList();
        int numSharedElements = sharedElementsIn.size();
        for (int i = 0; i < numSharedElements; i++) {
            View view = (View) sharedElementsIn.get(i);
            names.add(view.getTransitionName());
            view.setTransitionName(null);
        }
        return names;
    }

    public static void setNameOverridesOptimized(View sceneRoot, ArrayList<View> sharedElementsOut, ArrayList<View> sharedElementsIn, ArrayList<String> inNames, Map<String, String> nameOverrides) {
        int numSharedElements = sharedElementsIn.size();
        ArrayList<String> outNames = new ArrayList();
        for (int i = 0; i < numSharedElements; i++) {
            View view = (View) sharedElementsOut.get(i);
            String name = view.getTransitionName();
            outNames.add(name);
            if (name != null) {
                view.setTransitionName(null);
                String inName = (String) nameOverrides.get(name);
                for (int j = 0; j < numSharedElements; j++) {
                    if (inName.equals(inNames.get(j))) {
                        ((View) sharedElementsIn.get(j)).setTransitionName(name);
                        break;
                    }
                }
            }
        }
        OneShotPreDrawListener.add(sceneRoot, new C00123(numSharedElements, sharedElementsIn, inNames, sharedElementsOut, outNames));
    }

    public static void captureTransitioningViews(ArrayList<View> transitioningViews, View view) {
        if (view.getVisibility() != 0) {
            return;
        }
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            if (viewGroup.isTransitionGroup()) {
                transitioningViews.add(viewGroup);
                return;
            }
            int count = viewGroup.getChildCount();
            for (int i = 0; i < count; i++) {
                captureTransitioningViews(transitioningViews, viewGroup.getChildAt(i));
            }
            return;
        }
        transitioningViews.add(view);
    }

    public static void findNamedViews(Map<String, View> namedViews, View view) {
        if (view.getVisibility() == 0) {
            String transitionName = view.getTransitionName();
            if (transitionName != null) {
                namedViews.put(transitionName, view);
            }
            if (view instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) view;
                int count = viewGroup.getChildCount();
                for (int i = 0; i < count; i++) {
                    findNamedViews(namedViews, viewGroup.getChildAt(i));
                }
            }
        }
    }

    public static void setNameOverridesUnoptimized(View sceneRoot, ArrayList<View> sharedElementsIn, Map<String, String> nameOverrides) {
        OneShotPreDrawListener.add(sceneRoot, new C00134(sharedElementsIn, nameOverrides));
    }

    private static String findKeyForValue(Map<String, String> map, String value) {
        for (Entry<String, String> entry : map.entrySet()) {
            if (value.equals(entry.getValue())) {
                return (String) entry.getKey();
            }
        }
        return null;
    }

    public static void scheduleRemoveTargets(Object overallTransitionObj, Object enterTransition, ArrayList<View> enteringViews, Object exitTransition, ArrayList<View> exitingViews, Object sharedElementTransition, ArrayList<View> sharedElementsIn) {
        ((Transition) overallTransitionObj).addListener(new C00145(enterTransition, enteringViews, exitTransition, exitingViews, sharedElementTransition, sharedElementsIn));
    }

    public static void swapSharedElementTargets(Object sharedElementTransitionObj, ArrayList<View> sharedElementsOut, ArrayList<View> sharedElementsIn) {
        TransitionSet sharedElementTransition = (TransitionSet) sharedElementTransitionObj;
        if (sharedElementTransition != null) {
            sharedElementTransition.getTargets().clear();
            sharedElementTransition.getTargets().addAll(sharedElementsIn);
            replaceTargets(sharedElementTransition, sharedElementsOut, sharedElementsIn);
        }
    }

    public static void replaceTargets(Object transitionObj, ArrayList<View> oldTargets, ArrayList<View> newTargets) {
        Transition transition = (Transition) transitionObj;
        int i;
        if (transition instanceof TransitionSet) {
            TransitionSet set = (TransitionSet) transition;
            int numTransitions = set.getTransitionCount();
            for (i = 0; i < numTransitions; i++) {
                replaceTargets(set.getTransitionAt(i), oldTargets, newTargets);
            }
        } else if (!hasSimpleTarget(transition)) {
            List<View> targets = transition.getTargets();
            if (targets != null && targets.size() == oldTargets.size() && targets.containsAll(oldTargets)) {
                int targetCount = newTargets == null ? 0 : newTargets.size();
                for (i = 0; i < targetCount; i++) {
                    transition.addTarget((View) newTargets.get(i));
                }
                for (i = oldTargets.size() - 1; i >= 0; i--) {
                    transition.removeTarget((View) oldTargets.get(i));
                }
            }
        }
    }

    public static void addTarget(Object transitionObj, View view) {
        if (transitionObj != null) {
            ((Transition) transitionObj).addTarget(view);
        }
    }

    public static void removeTarget(Object transitionObj, View view) {
        if (transitionObj != null) {
            ((Transition) transitionObj).removeTarget(view);
        }
    }

    public static void setEpicenter(Object transitionObj, Rect epicenter) {
        if (transitionObj != null) {
            ((Transition) transitionObj).setEpicenterCallback(new C00156(epicenter));
        }
    }

    public static void scheduleNameReset(ViewGroup sceneRoot, ArrayList<View> sharedElementsIn, Map<String, String> nameOverrides) {
        OneShotPreDrawListener.add(sceneRoot, new C00167(sharedElementsIn, nameOverrides));
    }
}
