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
    }
    // Verificar y escribir encabezados siempre en la primera fila
    var firstRow = sheet.getRange(1, 1, 1, headerRow.length).getValues()[0];
    var headersOk = true;
    for (var i = 0; i < headerRow.length; i++) {
      if (firstRow[i] !== headerRow[i]) {
        headersOk = false;
        break;
      }
    }
    if (!headersOk) {
      sheet.getRange(1, 1, 1, headerRow.length).setValues([headerRow]);
    }

    var action = e.parameter.action || '';
    var codigoReporte = e.parameter.codigo_reporte || '';
    
    // Consultar reporte por código
    if (action === 'consultar' && codigoReporte) {
      var data = sheet.getDataRange().getValues();
      for (var i = 1; i < data.length; i++) {
        if (data[i][0] == codigoReporte) {
          // SOLUCIÓN DIRECTA: Obtenemos las URLs de las fotos
          var fotosStr = data[i][8] || '';
          var fotosUrls = "";
          
          // Obtenemos el valor de la celda como una fórmula para procesarla correctamente
          if (fotosStr) {
            try {
              var row = i + 1; // +1 porque data es 0-indexed pero las filas de sheet empiezan en 1
              var fotosCell = sheet.getRange(row, 9); // Columna 9 es "Fotos" (columna I)
              var formulas = fotosCell.getFormulas();
              
              if (formulas && formulas.length > 0 && formulas[0].length > 0) {
                var cellFormula = formulas[0][0]; // Primera fila, primera columna de las seleccionadas
                
                // Si es una fórmula HYPERLINK
                if (cellFormula && cellFormula.indexOf('HYPERLINK') >= 0) {
                  // Extraemos las URLs
                  var extractedUrls = [];
                  var formulaLines = cellFormula.split('\n');
                  
                  for (var j = 0; j < formulaLines.length; j++) {
                    var line = formulaLines[j];
                    var linkMatch = line.match(/HYPERLINK\s*\(\s*"([^"]+)"/);
                    if (linkMatch && linkMatch.length > 1) {
                      extractedUrls.push(linkMatch[1]);
                    }
                  }
                  
                  fotosUrls = extractedUrls.join(',');
                }
              }
              
              // Si aún no tenemos URLs, intentamos buscar HTML si es que se guardó como HTML
              if (!fotosUrls && fotosStr.indexOf('<a href=') >= 0) {
                var extractedUrls = [];
                var linkMatches = fotosStr.match(/<a href="([^"]+)"/g);
                if (linkMatches) {
                  for (var j = 0; j < linkMatches.length; j++) {
                    var match = linkMatches[j].match(/<a href="([^"]+)"/);
                    if (match && match.length > 1) {
                      extractedUrls.push(match[1]);
                    }
                  }
                  fotosUrls = extractedUrls.join(',');
                }
              }
            } catch (error) {
              Logger.log('Error extrayendo URLs: ' + error);
              // Si hay error dejamos fotosUrls vacío
            }
          }
          
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
            fotos: fotosUrls,
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
      // Recibe los parámetros exactamente como los envía la app
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
      
      // Usar SIEMPRE punto y coma como separador en HYPERLINK
      var sep = ";";

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
              linksArr.push('=HYPERLINK("' + url + '"' + sep + ' "Ver foto ' + (j+1) + '")');
            }
          }
          fotosLinks = linksArr.join('\n');
        }
        
        // Agrega los datos en el mismo orden que el headerRow
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