function plot(points){
	document.write( '<script language="javascript" src="../jqplot/jquery.min.js" />' );
	document.write( '<script language="javascript" src="../jqplot/jquery.jqplot.min.js" />' );
	var plot3 = $.jqplot('chartdiv', [points], {  
		series:[{showMarker:false}],
		axes:{
			xaxis:{
				label:'Angle (radians)',
				labelRenderer: $.jqplot.CanvasAxisLabelRenderer,
				labelOptions: {
					fontFamily: 'Georgia, Serif',
					fontSize: '12pt'
				}
			},
			yaxis:{
				label:'Cosine',
				labelRenderer: $.jqplot.CanvasAxisLabelRenderer,
				labelOptions: {
					fontFamily: 'Georgia, Serif',
					fontSize: '12pt'
				}
			}
		}
	});
}
