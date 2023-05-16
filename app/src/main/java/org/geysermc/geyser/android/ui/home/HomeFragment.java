/*
 * Copyright (c) 2020-2020 GeyserMC. http://geysermc.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * @author GeyserMC
 * @link https://github.com/GeyserMC/GeyserAndroid
 */

package org.geysermc.geyser.android.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;

import org.geysermc.geyser.android.R;

public class HomeFragment extends Fragment {

    private NavigationView navView;
    private NavController navController;
    private Menu menu;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        Button btnJoinBE = root.findViewById(R.id.btnJoinBE);

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Get the menu and nav controller
        navView = requireActivity().findViewById(R.id.nav_view);
        if (navView == null) {
            throw new IllegalStateException("Nav view not found");
        }

        menu = navView.getMenu();
        if (menu == null) {
            throw new IllegalStateException("Menu not found");
        }

        NavHostFragment parentFragment = (NavHostFragment) getParentFragment();
        if (parentFragment == null) {
            throw new IllegalStateException("Parent fragment not found");
        }

        navController = parentFragment.getNavController();
        if (navController == null) {
            throw new IllegalStateException("Nav controller not found");
        }

        // Setup the join BE button
        Button btnJoinBE = requireView().findViewById(R.id.btnJoinBE);
        btnJoinBE.setOnClickListener(v -> NavigationUI.onNavDestinationSelected(menu.findItem(R.id.nav_proxy), navController));
    }
}
