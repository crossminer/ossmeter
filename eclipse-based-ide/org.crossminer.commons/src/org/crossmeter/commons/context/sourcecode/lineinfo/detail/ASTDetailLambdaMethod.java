/*********************************************************************
* Copyright (c) 2017 FrontEndART Software Ltd.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*    Zsolt J�nos Szamosv�lgyi
*    Endre Tam�s V�radi
*    Gerg� Balogh
**********************************************************************/
package org.crossmeter.commons.context.sourcecode.lineinfo.detail;

import java.util.List;

/**
 * Provides informations about a lambda expression's method.
 * @see ASTDetailMethod
 *
 */
public class ASTDetailLambdaMethod extends ASTDetailMethod {
	
	public ASTDetailLambdaMethod(int startChar, int endChar, String signature,
			List<ASTResolvedAnnotation> annotations) {
		super(startChar, endChar, ASTDetailKind.LAMBDAMETHOD, annotations, signature);
	}
}
