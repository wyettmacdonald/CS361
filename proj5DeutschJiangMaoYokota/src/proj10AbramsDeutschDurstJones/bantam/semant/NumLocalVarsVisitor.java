/*
 * File: bantam.semant.NumLocalVarsVisitor.java
 * CS361 Project 11
 * Names: Martin Deutsch and Wyett MacDonald
 * Date: 2/13/2019
 * This file extends Visitor to find and store the local variables
 * in each method in a given AST
 */

package proj10AbramsDeutschDurstJones.bantam.semant;
import proj10AbramsDeutschDurstJones.bantam.ast.*;
import proj10AbramsDeutschDurstJones.bantam.visitor.Visitor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * NumLocalVarsVisitor searches an AST for local variables
 * and stores them in a hashmap
 *
 * @author Martin Deutsch
 * @author Wyett MacDonald
 */
public class NumLocalVarsVisitor extends Visitor {
}