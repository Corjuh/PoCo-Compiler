package com.poco.PoCoRuntime;

/**
 * Created by caoyan on 12/14/14.
 */
public class BopSRE extends SRE {
	// SRE Binary Operators: Union; Conjunction; Disjunction; Equals; Punion
	private String srebop;
	private SRE sre1;
	private SRE sre2;

	public SRE getSre1() {
		return sre1;
	}

	public void setSre1(SRE sre1) {
		this.sre1 = sre1;
	}

	public SRE getSre2() {
		return sre2;
	}

	public void setSre2(SRE sre2) {
		this.sre2 = sre2;
	}

	public void setSrebop(String srebop) {
		this.srebop = srebop;
	}

	public String getSrebop() {
		return srebop;
	}

	public BopSRE(String srebop, SRE sre1, SRE sre2) {
		this.srebop = srebop;
		this.sre1 = sre1;
		this.sre2 = sre2;
	}

	/**
	 * return the complement value of a BopSRE. which includes: a) both is sre1
	 * and sre2 should be set as their complement value b) its SRE value need to
	 * be set as its complement c) its posSRES value and negSREs need to be
	 * switched Variables cases do not need to be handled.
	 */
	@Override
	public SRE complement() {
		SRE temp = this.getAbsVal();
		return temp.complement();
	}

	/**
	 * Includes only the actions in SRE In this function, the variable value
	 * will be loaded in order to know whether it is an action or not.
	 *
	 * @return
	 */
	@Override
	public SRE getAction() {
		SRE temp = this.getAbsVal();
		return temp.getAction();
	}

	/**
	 * Includes only the result in SRE
	 *
	 * @return
	 */
	@Override
	public SRE getResult() {
		SRE temp = this.getAbsVal();
		return temp.getResult();
	}

	/**
	 * Includes only positive portion of SRE
	 */
	@Override
	public SRE getPostiveVal() {
		SRE temp = this.getAbsVal();
		return temp.getPostiveVal();
	}

	/**
	 * Includes only negative portion of SRE
	 *
	 * @return
	 */
	@Override
	public SRE getNegativeVal() {
		SRE temp = this.getAbsVal();
		return temp.getNegativeVal();
	}

	@Override
	public CalculatedSRE convert2CalculatedSre() {
		return (CalculatedSRE) this.getAbsVal();
	}

	/**
	 * get the absolute value of this SRE, that is, apply all the variable
	 * values.
	 *
	 * @param mode
	 *            0: get the SRE absolute value, that is, apply all the variable
	 *            values; 1: apply only necessary variables, that is, a. for
	 *            action case, will only apply the variable as method name b.
	 *            for result case, will not apply the variable value
	 * @return
	 */
	@Override
	public SRE getAbsVal() {
		SRE val4SRE1 = this.sre1.getAbsVal();
		SRE val4SRE2 = this.sre2.getAbsVal();

		return SREUtil.performBOPs(this.srebop, val4SRE1, val4SRE2);
	}

	@Override
	public String toString() {
		return "BopSRE [srebop=" + srebop + ", sre1=" + sre1 + ", sre2=" + sre2
				+ "]";
	}
}