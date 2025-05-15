function doGet(e) {
  try {
    var headerRow = [
      "Código Reporte", "Nombre Interesado", "Colonia", "Dirección", "Celular", "Correo",
      "Tipo Reporte", "Descripción", "Fotos", "Fecha y Hora", "Estado", "Mensaje del Administrador"
    ];
    
    var ss = SpreadsheetApp.getActiveSpreadsheet();
    var sheet = ss.getSheetByName("Reportes");
    
    // Crear la hoja si no existe
    if (!sheet) {
      sheet = ss.insertSheet("Reportes");
      sheet.appendRow(headerRow);
    } else if (sheet.getLastRow() === 0) {
      sheet.appendRow(headerRow);
    }

    // Determinar la acción a realizar
    var action = e.parameter.action || '';
    var codigoReporte = e.parameter.codigo_reporte || '';
    
    // Consultar reporte por código
    if (action === 'consultar' && codigoReporte) {
      var data = sheet.getDataRange().getValues();
      for (var i = 1; i < data.length; i++) {
        if (data[i][0] == codigoReporte) {
          // Devolver todos los datos del reporte
          var respuesta = {
            result: "success",
            codigo_reporte: data[i][0],
            nombre_interesado: data[i][1],
            colonia: data[i][2],
            direccion: data[i][3],
            celular: data[i][4],
            correo: data[i][5],
            tipo_reporte: data[i][6],
            descripcion: data[i][7],
            fotos: data[i][8],
            fechaHora: data[i][9],
            estado: data[i][10],
            mensaje: data[i][11]
          };
          return ContentService.createTextOutput(JSON.stringify(respuesta))
            .setMimeType(ContentService.MimeType.JSON);
        }
      }
      return ContentService.createTextOutput(JSON.stringify({
        result: "error",
        message: "Reporte no encontrado"
      })).setMimeType(ContentService.MimeType.JSON);
    }
    // Crear un nuevo reporte
    else if (action === 'crear' && codigoReporte) {
      var nombreInteresado = e.parameter.nombre_interesado || '';
      var colonia = e.parameter.colonia || '';
      var direccion = e.parameter.direccion || '';
      var celular = e.parameter.celular || '';
      var correo = e.parameter.correo || '';
      var tipoReporte = e.parameter.tipo_reporte || '';
      var descripcion = e.parameter.descripcion || '';
      var fotos = e.parameter.fotos || '';
      var fechaHora = Utilities.formatDate(new Date(), Session.getScriptTimeZone(), "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
      var estado = "Pendiente";
      var mensajeAdmin = "Su reporte ha sido recibido y está siendo procesado";

      // Verificar si el código ya existe para evitar duplicados
      var data = sheet.getDataRange().getValues();
      var reporteExistente = false;
      for (var i = 1; i < data.length; i++) {
        if (data[i][0] == codigoReporte) {
          reporteExistente = true;
          break;
        }
      }
      
      // Solo agregar si el código no existe y los campos requeridos no están vacíos
      if (!reporteExistente && codigoReporte.trim() !== "" && nombreInteresado.trim() !== "") {
        // Procesar los links de fotos para guardarlos como HYPERLINKS
        var fotosLinks = "";
        if (fotos.trim() !== "") {
          var fotosArr = fotos.split(",");
          var linksArr = [];
          for (var j = 0; j < fotosArr.length; j++) {
            var url = fotosArr[j].trim();
            if (url !== "") {
              linksArr.push('=HYPERLINK("' + url + '", "Ver foto ' + (j+1) + '")');
            }
          }
          fotosLinks = linksArr.join('\n');
        }
        
        sheet.appendRow([
          codigoReporte,
          nombreInteresado,
          colonia,
          direccion,
          celular,
          correo,
          tipoReporte,
          descripcion,
          fotosLinks,
          fechaHora,
          estado,
          mensajeAdmin
        ]);
      }
      
      return ContentService.createTextOutput(JSON.stringify({
        result: "success",
        message: "Reporte guardado correctamente",
        codigo_reporte: codigoReporte
      })).setMimeType(ContentService.MimeType.JSON);
    }
    // Si no se especifica acción o es inválida
    else {
      return ContentService.createTextOutput(JSON.stringify({
        result: "error",
        message: "Parámetros incorrectos. Se requiere action y codigo_reporte."
      })).setMimeType(ContentService.MimeType.JSON);
    }
  } catch (error) {
    return ContentService.createTextOutput(JSON.stringify({
      result: "error",
      message: error.toString()
    })).setMimeType(ContentService.MimeType.JSON);
  }
}

function doPost(e) {
  return doGet(e);
} 