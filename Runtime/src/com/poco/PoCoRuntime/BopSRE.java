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

	public BopSRE(String srebop, SRE sre1, SRE sre2) {
		this.srebop = srebop;
		this.sre1 = sre1;
		this.sre2 = sre2;
	}

	public void setSRE1(SRE sre1) {
		this.sre1 = sre1;
	}

	public void setSRE2(SRE sre2) {
		this.sre2 = sre2;
	}

	@Override
	/**
	 * return the complement value of a BopSRE. which includes:
	 * a) both is sre1 and sre2 should be set as their complement value
	 * b) its SRE value need to be set as its complement
	 * c) its posSRES value and negSREs need to be switched
	 * Variable case can be ignore here
	 */
	public SRE complement() {
		return (SREUtil.performBOPs(this.srebop, sre1, sre2)).complement();
	}

	/**
	 * Includes only the actions in SRE
	 *
	 * @return
	 */
	@Override
	public SRE getAction() {
		return (SREUtil.performBOPs(this.srebop, sre1, sre2)).getAction();
	}

	/**
	 * Includes only the result in SRE
	 *
	 * @return
	 */
	@Override
	public SRE getResult() {
		return (SREUtil.performBOPs(this.srebop, sre1, sre2)).getResult();
	}

	@Override
	/**
	 * Includes only positive portion of SRE
	 */
	public SRE getPostiveVal() {
		return (SREUtil.performBOPs(this.srebop, sre1, sre2)).getPostiveVal();
	}

	/**
	 * Includes only negative portion of SRE
	 *
	 * @return
	 */
	public SRE getNegativeVal() {
		return (SREUtil.performBOPs(this.srebop, sre1, sre2)).getNegativeVal();
	}

	public String getSrebop() {
		return srebop;
	}

}