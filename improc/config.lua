localPath         = {
getLocalPath = function ()
                  return '~/NetBeansProjects/guidog/improc/'
               end
}

local config = {
  batchSize         = 3,
  momentum          = 0,
  learningRate      = 1e-2,
  weightDecay       = 1e-5,
  learningRateDecay = 1e-7,
  epochnm           = 15,
  modelPath         = localPath.getLocalPath() .. 'models/',
  with_plotting     = true,
  data_file_path    = localPath.getLocalPath() .. 'data/save.dat',
  pathToImages      = localPath.getLocalPath() .. 'images/',
  pathToTestImages  = localPath.getLocalPath() .. 'output/',
  categories        = {"button", "checkbox", "input", "other"},
  imagesSize        = {x = 200, y = 30},
  channels          = 1,
  trainPortion      = 0.7,
  numImages         = 10
}



return config
