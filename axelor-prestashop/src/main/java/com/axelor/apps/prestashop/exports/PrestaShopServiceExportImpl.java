/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.prestashop.exports;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.ZonedDateTime;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.tika.io.IOUtils;
import org.xml.sax.SAXException;

import com.axelor.apps.base.db.AppPrestashop;
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.prestashop.exports.service.ExportAddressService;
import com.axelor.apps.prestashop.exports.service.ExportCategoryService;
import com.axelor.apps.prestashop.exports.service.ExportCountryService;
import com.axelor.apps.prestashop.exports.service.ExportCurrencyService;
import com.axelor.apps.prestashop.exports.service.ExportCustomerService;
import com.axelor.apps.prestashop.exports.service.ExportOrderService;
import com.axelor.apps.prestashop.exports.service.ExportProductService;
import com.axelor.apps.prestashop.service.library.PrestaShopWebserviceException;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class PrestaShopServiceExportImpl implements PrestaShopServiceExport {

	@Inject
	private MetaFiles metaFiles;

	@Inject
	private ExportCurrencyService currencyService;

	@Inject
	private ExportCountryService countryService;

	@Inject
	private ExportCustomerService customerService;

	@Inject
	private ExportAddressService addressService;

	@Inject
	private ExportCategoryService categoryService;

	@Inject
	private ExportProductService productService;

	@Inject
	private ExportOrderService orderService;

	/**
	 * Export axelor base module
	 *
	 * @param endDate
	 * @throws PrestaShopWebserviceException
	 * @throws TransformerException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws JAXBException
	 * @throws TransformerFactoryConfigurationError
	 */
	public void exportAxelorBase(AppPrestashop appConfig, ZonedDateTime endDate, final BufferedWriter logWriter) throws PrestaShopWebserviceException, TransformerException, IOException, ParserConfigurationException, SAXException, JAXBException, TransformerFactoryConfigurationError {
		currencyService.exportCurrency(appConfig, endDate, logWriter);
		countryService.exportCountry(appConfig, endDate, logWriter);
		customerService.exportCustomer(appConfig, endDate, logWriter);
		addressService.exportAddress(appConfig, endDate, logWriter);
		categoryService.exportCategory(appConfig, endDate, logWriter);
		productService.exportProduct(appConfig, endDate, logWriter);
	}

	/**
	 * Export Axelor modules (Base, SaleOrder)
	 */
	@Override
	public void export(AppPrestashop appConfig, ZonedDateTime endDate, Batch batch) throws PrestaShopWebserviceException, TransformerException, IOException, ParserConfigurationException, SAXException, JAXBException, TransformerFactoryConfigurationError {
		StringBuilderWriter logWriter = new StringBuilderWriter(1024);
		// FIXME can be dropped at the end of refactoring (only here to keep newLine())
		BufferedWriter bufferedLogWriter = new BufferedWriter(logWriter);
		try {

			exportAxelorBase(appConfig, endDate, bufferedLogWriter);

			orderService.exportOrder(appConfig, endDate, bufferedLogWriter);
			bufferedLogWriter.write(String.format("%n==== END OF LOG ====%n"));
		} finally {
			IOUtils.closeQuietly(bufferedLogWriter);
			MetaFile exporMetaFile = metaFiles.upload(new ByteArrayInputStream(logWriter.toString().getBytes()), "export-log.txt");
			batch.setPrestaShopBatchLog(exporMetaFile);
		}
	}
}
