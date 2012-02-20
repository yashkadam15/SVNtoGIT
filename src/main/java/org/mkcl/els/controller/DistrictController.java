/**
 * See the file LICENSE for redistribution information.
 *
 * Copyright (c) 2011 MKCL.  All rights reserved.
 *
 * Project: e-Legislature
 * File: org.mkcl.els.controller.DistrictController.java
 * Created On: Dec 19, 2011
 */

package org.mkcl.els.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.Valid;

import org.mkcl.els.common.editors.StateEditor;
import org.mkcl.els.domain.CustomParameter;
import org.mkcl.els.domain.District;
import org.mkcl.els.domain.Grid;
import org.mkcl.els.domain.State;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * The Class DistrictController.
 *
 * @author nileshp
 * @since v1.0.0
 */
@Controller
@RequestMapping("/districts")
public class DistrictController extends BaseController {

    /**
     * Gets the module page.
     *
     * @param model the model
     * @return the string
     */
    @RequestMapping(value = "module", method = RequestMethod.GET)
    public String index(final ModelMap model) {
        return "masters/districts/module";
    }

    /**
     * Index.
     *
     * @param model the model
     * @return the string
     */
    @RequestMapping(value = "list", method = RequestMethod.GET)
    public final String list(final ModelMap model) {
        Grid grid = Grid.findByName("DISTRICT_GRID");
        model.addAttribute("gridId", grid.getId());
        return "masters/districts/list";
    }

    /**
     * New form.
     *
     * @param model the model
     * @param locale the locale
     * @return the string
     */
    @RequestMapping(value = "new", method = RequestMethod.GET)
    public final String newForm(final ModelMap model, final Locale locale) {
        District district = new District();
        district.setLocale(locale.toString());
        populateModel(
                model, district, CustomParameter.findByName("DEFAULT_STATE")
                .getValue());
        return "masters/districts/new";
    }

    /**
     * Edits the.
     *
     * @param id the id
     * @param model the model
     * @return the string
     */
    @RequestMapping(value = "{id}/edit", method = RequestMethod.GET)
    public final String edit(@PathVariable final Long id, final ModelMap model) {
        final District district = District.findById(id);
        populateModel(model, district, district.getState().getName());
        return "masters/districts/edit";
    }

    /**
     * Creates the new District.
     *
     * @param district the district
     * @param result the result
     * @param model the model
     * @param redirectAttributes the redirect attributes
     * @return the string
     */
    @RequestMapping(method = RequestMethod.POST)
    public final String create(@Valid @ModelAttribute("district") final District district,
            final BindingResult result,
            final ModelMap model,
            final RedirectAttributes redirectAttributes) {
        this.validate(district, result);
        if (result.hasErrors()) {
            populateModel(model, district, district.getState().getName());
            model.addAttribute("type", "error");
            model.addAttribute("msg", "create_failed");
            return "masters/districts/new";
        }
        district.persist();
        redirectAttributes.addFlashAttribute("type", "success");
        redirectAttributes.addFlashAttribute("msg", "create_success");
        return "redirect:districts/" + district.getId() + "/edit?type=success&msg=create_success";

    }

    /**
     * Edits the.
     *
     * @param district the district
     * @param result the result
     * @param model the model
     * @param redirectAttributes the redirect attributes
     * @return the string
     */
    @RequestMapping(method = RequestMethod.PUT)
    public final String edit(@Valid @ModelAttribute("district") final District district,
            final BindingResult result,
            final ModelMap model, final RedirectAttributes redirectAttributes) {
        this.validate(district, result);
        if (result.hasErrors()) {
            populateModel(model, district, district.getState().getName());
            model.addAttribute("type", "error");
            model.addAttribute("msg", "update_failed");
            return "masters/districts/edit";
        }
        district.update();
        redirectAttributes.addFlashAttribute("type", "success");
        redirectAttributes.addFlashAttribute("msg", "update_success");
        return "redirect:districts/" + district.getId()
                + "/edit?type=success&msg=update_success";

    }

    /**
     * Delete.
     *
     * @param id the id
     * @param model the model
     * @return the string
     */
    @RequestMapping(value = "{id}/delete", method = RequestMethod.DELETE)
    public final String delete(@PathVariable final Long id, final ModelMap model) {
        final District district = District.findById(id);
        district.remove();
        model.addAttribute("type", "success");
        model.addAttribute("msg", "delete_success");
        return "info";
    }

    /**
     * Validate.
     *
     * @param district the district
     * @param errors the errors
     */
    private void validate(final District district, final Errors errors) {
        if (district.getName() != null) {
            if (district.getLocale().equals("en")) {
                String name = district.getName();
                Pattern pattern = Pattern.compile("[A-Za-z ]{1,50}");
                Matcher matcher = pattern.matcher(name);
                if (!matcher.matches()) {
                    errors.rejectValue("name", "Pattern");
                }
                if (name.length() > 100 || name.length() < 1) {
                    errors.rejectValue("name", "Size");

                }
            }
        }
        District duplicateParameter = District.findByName(district.getName());
        if (duplicateParameter != null) {
            if (!duplicateParameter.getId().equals(district.getId())) {
                errors.rejectValue("name", "NonUnique");
            }
        }
        if (district.getId() != null) {
            /*
             * if (!district.getVersion().equals(
             * District.findById(district.getId()).getVersion())) {
             * errors.reject("name", "Version_Mismatch"); }
             */
            if (!district.checkVersion()) {
                errors.reject("version", "Version_Mismatch");
            }
        }
    }

    /**
     * Inits the binder.
     *
     * @param binder the binder
     */
    @InitBinder
    public final void initBinder(final WebDataBinder binder) {
        binder.registerCustomEditor(State.class, new StateEditor());
    }

    /**
     * Populate model.
     *
     * @param model the model
     * @param district the district
     * @param stateName the state name
     */
    private void populateModel(final ModelMap model,
            final District district,
            final String stateName) {
        List<State> states = State.findAllSorted(
                "name", district.getLocale(), false);
        State selectedState = State.findByName(stateName);
        List<State> newStates = new ArrayList<State>();
        newStates.add(selectedState);
        states.remove(selectedState);
        newStates.addAll(states);
        model.addAttribute("district", district);
        model.addAttribute("states", newStates);
    }

}
