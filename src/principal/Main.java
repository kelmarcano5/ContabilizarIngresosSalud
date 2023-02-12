package principal;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;

public class Main {

	public static void main(String[] args) throws Throwable {
		// TODO Auto-generated method stub
		
		int result=0;
		ControlDB control = new ControlDB();
		control.cn.setAutoCommit(false);
		
		int smn_documento_id;
		int smn_entidades_rf;
		int smn_sucursales_rf;
		int smn_moneda_rf;
		int smn_documento_general_rf; 
		int ccc_numero_documento;
		int smn_control_cierre_contable_id;
		int smn_clase_auxiliar_id;
		int smn_auxiliar_id;
		int smn_tasa;
		int smn_modulo_rf;
		int smn_tipo_documento_id;
		int smn_rel_modulo_documento_tipo_doc_id;
		int doc_numero_documento;
		int smn_clase_auxiliar_ord_rf;
		int smn_auxiliar_ord_rf;
		int doc_orden_compra_rf;
		int smn_centro_costo_rf;
		int smn_proyecto_rf;
		int smn_documentos_generales_rf_afecta;
		int doc_numero_doc_afecta;
		int doc_numero_control_doc_afect;
		int smn_codigos_impuestos_rf;
		int doc_numero_control_fiscal_inicial;
		int doc_numero_control_fiscal_ultimo;
		int doc_numero_control1_inicial;
		int doc_numero_control1_ultimo;
		int doc_numero_control2_inicial;
		int doc_numero_control2_ultimo;
		int doc_ano_comprobante;
		int doc_periodos_detalles_rf;
		int smn_tipo_comprobante_id;
		int doc_num_comprobante;
		int smn_elemento_rf;
		int smn_documento_id_cont;
		double igs_monto_ingreso_ml=0;
		double igs_monto_ingreso_ma=0;
		double doc_tasa_cambio;
		java.sql.Date igs_fecha_registro=null;
		java.sql.Date doc_fecha_rec;
		java.sql.Date doc_fecha_vcto;
		java.sql.Date doc_fecha_doc_afecta;
		java.sql.Date doc_fecha_comprobante;
		String ccc_estatus;
		String ccc_idioma;
		String ccc_usuario;
		String doc_estatus;
		String doc_planilla_importacion;
		String doc_numero_control;
		String dcg_descripcion;
		
		SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd");
		String fechaActual= sdformat.format(new Date());
		
		String sistemaOperativo = System.getProperty("os.name");
		String file;
		  
		if(sistemaOperativo.equals("Windows 7") || sistemaOperativo.equals("Windows 8") || sistemaOperativo.equals("Windows 10")) 
			file =  "C:/log/logContabilizarIngresosSalud_"+fechaActual+".txt";
		else
			file = "./logContabilizarIngresosSalud_"+fechaActual+".txt";
		
		File newLogFile = new File(file);
		FileWriter fw;
		String str="";
		
		if(!newLogFile.exists())
			fw = new FileWriter(newLogFile);
		else
			fw = new FileWriter(newLogFile,true);
		
		BufferedWriter bw=new BufferedWriter(fw);
		
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		
		SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
		Date parsed = date.parse(fechaActual);
        java.sql.Date sqlDate = new java.sql.Date(parsed.getTime());
        
        LocalDateTime locaDate = LocalDateTime.now();
        int hours  = locaDate.getHour();
        int minutes = locaDate.getMinute();
        int seconds = locaDate.getSecond();
        String hora = hours+":"+minutes+":"+seconds;
        
		try
		{
			str = "----------"+timestamp+"----------";	
			bw.write(str);
			bw.flush();
			bw.newLine();
			bw.newLine();
			
			str = "--- INICIO DEL PROCESO: 'Contabilizar Ingreso Salud' ---";	
			bw.write(str);
			bw.flush();
			bw.newLine();
			
			str = "--- Consultando Ingresos para generar cierre contable ---";	
			bw.write(str);
			bw.flush();
			bw.newLine();
			
			ResultSet ingresos=control.consultarIngresos();
			
			if(getRecordCount(ingresos)>0)
			{
				
				while(ingresos.next())
				{	
					igs_fecha_registro=ingresos.getDate("igs_fecha_registro");							
					smn_entidades_rf=ingresos.getInt("smn_entidades_rf");		
					smn_sucursales_rf=ingresos.getInt("smn_sucursales_rf");		
					smn_documento_id=ingresos.getInt("smn_documento_id");
					smn_moneda_rf=ingresos.getInt("smn_moneda_rf");
					smn_clase_auxiliar_id=ingresos.getInt("smn_clase_auxiliar_id");
					smn_auxiliar_id=ingresos.getInt("smn_auxiliar_id");
					igs_monto_ingreso_ml=ingresos.getDouble("total_monto_ingreso_ml");
					igs_monto_ingreso_ma=ingresos.getDouble("total_monto_ingreso_ma");
					dcg_descripcion=ingresos.getString("dcg_descripcion");
					
					ResultSet documento_general=control.consultarDocGeneral(smn_documento_id);
					
					if(getRecordCount(documento_general)>0)
					{
						documento_general.next();
						if(documento_general.getString("smn_documento_general_rf")!=null)
						{
							smn_documento_general_rf=documento_general.getInt("smn_documento_general_rf");
						}	
						else
						{
							str = "--- El documento general esta vacio ---";	
							bw.write(str);
							bw.flush();
							bw.newLine();
							result=1;
							break;
						}
					}
					else
					{
						str = "--- No se encontro el documento de los Ingresos ---";	
						bw.write(str);
						bw.flush();
						bw.newLine();
						result=1;
						break;
					}
					
					ResultSet numero_cierre=control.consultarNumero_ccc();
					
					if(getRecordCount(numero_cierre)>0)
					{
						numero_cierre.next();
						if(numero_cierre.getString("ccc_numero_documento")!=null)
							ccc_numero_documento=numero_cierre.getInt("ccc_numero_documento")+1;
						else
							ccc_numero_documento=1;
					}
					else
					{
						ccc_numero_documento=1;
					}
					
					smn_tasa=0;
					ccc_estatus="CO";
					ccc_idioma="es";
					ccc_usuario="admin";
					
					ResultSet moduloSalud=control.moduloSalud();
					
					if(getRecordCount(moduloSalud)>0)
					{
						moduloSalud.next();
						smn_modulo_rf=moduloSalud.getInt("smn_modulos_id");
					}
					else
					{
						str = "--- No se encontro el modulo SMN_ADM=ADMISION ---";	
						bw.write(str);
						bw.flush();
						bw.newLine();
						result=1;
						break;
					}
					
					str = "--- Registrando cierre contable ---";	
					bw.write(str);
					bw.flush();
					bw.newLine();
					
					ResultSet RegistrarControlCierre=control.insertControl_cierre(smn_entidades_rf, smn_sucursales_rf, smn_documento_general_rf, smn_documento_id, igs_fecha_registro, ccc_numero_documento, igs_monto_ingreso_ml, igs_monto_ingreso_ma, smn_moneda_rf, smn_tasa, ccc_estatus, ccc_idioma, ccc_usuario, sqlDate, hora, smn_modulo_rf,smn_clase_auxiliar_id,smn_auxiliar_id);
					RegistrarControlCierre.next();
					
					smn_control_cierre_contable_id=RegistrarControlCierre.getInt("smn_control_cierre_contable_id");
					
					str = "--- Cierre contable registrado ---";	
					bw.write(str);
					bw.flush();
					bw.newLine();
					
					str = "--- Actualizando facturas con el cierre contable registrado ---";	
					bw.write(str);
					bw.flush();
					bw.newLine();
					
					control.updateIngresoCabecera(smn_entidades_rf, smn_sucursales_rf, smn_documento_id, smn_moneda_rf, igs_fecha_registro, smn_control_cierre_contable_id);
					
					str = "--- Ingresos actualizados ---";	
					bw.write(str);
					bw.flush();
					bw.newLine();
					
					ResultSet tipoDocumento=control.tipoDocumento(smn_modulo_rf, smn_documento_general_rf);
					
					if(getRecordCount(tipoDocumento)>0)
					{
						tipoDocumento.next();
						smn_tipo_documento_id=tipoDocumento.getInt("smn_tipo_documento_id");
						smn_rel_modulo_documento_tipo_doc_id=tipoDocumento.getInt("smn_rel_modulo_documento_tipo_doc_id");
					}
					else
					{
						str = "--- No se encontro el tipo de documento contable ---";	
						bw.write(str);
						bw.flush();
						bw.newLine();
						result=1;
						break;
					}
					
					ResultSet numeroDoc=control.numeroDocumento();
					
					if(getRecordCount(numeroDoc)>0)
					{
						numeroDoc.next();
						if(numeroDoc.getString("doc_numero_documento")!=null)
							doc_numero_documento=numeroDoc.getInt("doc_numero_documento")+1;
						else
							doc_numero_documento=1;
					}
					else
					{
						doc_numero_documento=1;
					}
					
					smn_clase_auxiliar_ord_rf=0;
					smn_auxiliar_ord_rf=0;
					doc_orden_compra_rf=0;
					smn_centro_costo_rf=0;
					smn_proyecto_rf=0;
					//doc_fecha_doc=sqlDate;
					doc_fecha_rec=sqlDate;
					doc_fecha_vcto=null;
					doc_planilla_importacion=null;
					smn_documentos_generales_rf_afecta=0;
					doc_numero_doc_afecta=0;
					doc_numero_control_doc_afect=0;
					doc_fecha_doc_afecta=null;
					smn_codigos_impuestos_rf=0;
					doc_numero_control_fiscal_inicial=0;
					doc_numero_control_fiscal_ultimo=0;
					doc_numero_control1_inicial=0;
					doc_numero_control1_ultimo=0;
					doc_numero_control2_inicial=0;
					doc_numero_control2_ultimo=0;
					doc_estatus="R";
					doc_ano_comprobante=0;
					doc_periodos_detalles_rf=0;

					ResultSet tipoComprobante=control.tipoComprobante(smn_rel_modulo_documento_tipo_doc_id);
					
					if(getRecordCount(tipoComprobante)>0)
					{
						tipoComprobante.next();
						smn_tipo_comprobante_id=tipoComprobante.getInt("mcc_tipo_comp");
					}
					else
					{
						str = "--- No se encontro el tipo de comprobante contable ---";	
						bw.write(str);
						bw.flush();
						bw.newLine();
						result=1;
						break;
					}
					
					doc_num_comprobante=0;
					doc_fecha_comprobante=null;
					doc_numero_control=null;
					smn_elemento_rf=0;
					//doc_descripcion=null;
					//smn_moneda_rf=0;
					
					if (igs_monto_ingreso_ml != 0 && igs_monto_ingreso_ma != 0 ){
						doc_tasa_cambio=igs_monto_ingreso_ml/igs_monto_ingreso_ma;
					}
					else {
						doc_tasa_cambio = 0;
					}
					
					
					str = "--- Registrando documento contable ---";	
					bw.write(str);
					bw.flush();
					bw.newLine();
					
					ResultSet insertDoc=control.insertDocumento(smn_modulo_rf, smn_entidades_rf, smn_sucursales_rf, 
							smn_documento_general_rf, smn_tipo_documento_id, doc_numero_documento, 
							smn_clase_auxiliar_id, smn_auxiliar_id, smn_clase_auxiliar_ord_rf, 
							smn_auxiliar_ord_rf, doc_orden_compra_rf, smn_centro_costo_rf, smn_proyecto_rf, 
							igs_fecha_registro, doc_fecha_rec, doc_fecha_vcto, doc_planilla_importacion, 
							igs_monto_ingreso_ml, igs_monto_ingreso_ma, doc_tasa_cambio, smn_documentos_generales_rf_afecta, 
							doc_numero_doc_afecta, doc_numero_control_doc_afect, doc_fecha_doc_afecta, 
							smn_codigos_impuestos_rf, doc_numero_control_fiscal_inicial, doc_numero_control_fiscal_ultimo, 
							doc_numero_control1_inicial, doc_numero_control1_ultimo, doc_numero_control2_inicial, 
							doc_numero_control2_ultimo, doc_estatus, doc_ano_comprobante, doc_periodos_detalles_rf, 
							smn_tipo_comprobante_id, doc_num_comprobante, "es", "admin", sqlDate, hora, doc_fecha_comprobante,
							doc_numero_control, smn_elemento_rf, dcg_descripcion, 0); 
					
									
					str = "--- Documento contable registrado ---";	
					bw.write(str);
					bw.flush();
					bw.newLine();
					
					insertDoc.next();
					smn_documento_id_cont=insertDoc.getInt("smn_documento_id_cont");
					
					str = "Datos para la consultar Ingresos" + "Entidad: " + smn_entidades_rf + " Sucursal: " + smn_sucursales_rf + " Documento_id; " +  smn_documento_id + " Moneda_rf: " +  smn_moneda_rf + " Fecha_registro: " + igs_fecha_registro;	
					bw.write(str);
					bw.flush();
					bw.newLine();
					
					str = "--- Actualizando estatus de los ingresos ---";	
					bw.write(str);
					bw.flush();
					bw.newLine();
						
					control.updateIngreso("CO", smn_entidades_rf, smn_sucursales_rf, smn_documento_id, smn_moneda_rf, igs_fecha_registro);
						
					str = "--- Estatus de los ingresos actualizados";	
					bw.write(str);
					bw.flush();
					bw.newLine();
					
				}//end while facturas
			}
			else
			{
				str = "*No se encontraron facturas para procesar*";	
				bw.write(str);
				bw.flush();
				bw.newLine();
				result=1;
			}
		}
		catch(Throwable e)
		{
			control.cn.rollback();
			throw e;
		}
		finally
		{
			if(result == 0)
			{
				control.cn.commit();
				str = "Cambios efectuados en la base de datos";	
				bw.write(str);
				bw.flush();
				bw.newLine();
			}
			else
			{
				control.cn.rollback();
				str = "Cambios no efectuados en la base de datos";	
				bw.write(str);
				bw.flush();
				bw.newLine();
			}
			
			if(control.cn!=null)
				control.cn.close();
			
			str = "FINAL DEL PROCESO";	
			bw.write(str);
			bw.flush();
			bw.newLine();
			
			bw.close();
		}
	}
	
	public static int getRecordCount(ResultSet rs)
	{
		int total=0;
		
		try {
			boolean ultimo = rs.last();
			
			if (ultimo)
			{
		        total = rs.getRow();
		        rs.beforeFirst();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return total;
	}
	
}
